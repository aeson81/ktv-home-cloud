package com.homektv.web;

import com.homektv.domain.SongFile;
import com.homektv.repo.SongFileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StreamControllerTest {

    @Test
    void openEndedRangeStreamsTheWholeRemainingFile() throws Exception {
        Path media = Files.createTempFile("home-ktv-stream-", ".mpg");
        try {
            byte[] content = new byte[2 * 1024 * 1024 + 17];
            for (int i = 0; i < content.length; i++) {
                content[i] = (byte) (i % 251);
            }
            Files.write(media, content);

            SongFile songFile = new SongFile();
            songFile.setFilePath(media.toString());
            songFile.setFormat("mpg");
            SongFileRepository repository = mock(SongFileRepository.class);
            when(repository.findById(1L)).thenReturn(Optional.of(songFile));

            var response = new StreamController(repository).stream(1L, "bytes=0-");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            StreamingResponseBody body = response.getBody();
            assertThat(body).isNotNull();
            body.writeTo(output);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PARTIAL_CONTENT);
            assertThat(response.getHeaders().getFirst("Content-Range"))
                    .isEqualTo("bytes 0-" + (content.length - 1) + "/" + content.length);
            assertThat(response.getHeaders().getContentLength()).isEqualTo(content.length);
            assertThat(output.toByteArray()).containsExactly(content);
        } finally {
            Files.deleteIfExists(media);
        }
    }
}
