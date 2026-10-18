package com.homektv;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationSafetyTest {

    private static final Pattern DESTRUCTIVE_STATEMENT = Pattern.compile(
            "(?im)^\\s*(DELETE\\s+FROM|TRUNCATE\\s+(?:TABLE\\s+)?|DROP\\s+(?:TABLE|COLUMN)\\s+)"
    );

    @Test
    void flywayMigrationsDoNotContainDestructiveStatements() throws Exception {
        Path migrationDirectory = migrationDirectory();
        List<String> violations;
        try (var files = Files.list(migrationDirectory)) {
            violations = files
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".sql"))
                    .filter(this::containsDestructiveStatement)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
        }

        assertThat(violations)
                .as("Flyway migrations must preserve user data; perform cleanup in audited application code")
                .isEmpty();
    }

    private boolean containsDestructiveStatement(Path path) {
        try {
            return DESTRUCTIVE_STATEMENT.matcher(Files.readString(path)).find();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read migration " + path, exception);
        }
    }

    private Path migrationDirectory() throws Exception {
        URI resource = getClass().getClassLoader().getResource("db/migration").toURI();
        return Paths.get(resource);
    }
}
