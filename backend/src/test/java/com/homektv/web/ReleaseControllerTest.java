package com.homektv.web;

import com.homektv.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ReleaseControllerTest {
    @Test
    void exposesVersionAnnouncementAndArchitecturePackages() throws Exception {
        var apk = Files.createTempFile("home-ktv-tv", ".apk");
        Files.write(apk, new byte[]{1, 2, 3});
        AppProperties properties = properties(apk.toUri().toString());
        MockMvc mvc = standaloneSetup(new ReleaseController(properties, new DefaultResourceLoader())).build();

        mvc.perform(get("/api/release"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.2.3"))
                .andExpect(jsonPath("$.versionCode").value(123))
                .andExpect(jsonPath("$.announcement.id").value("1.2.3"))
                .andExpect(jsonPath("$.tv.armeabiV7a.available").value(true))
                .andExpect(jsonPath("$.tv.arm64V8a.available").value(true));
    }

    @Test
    void downloadsApkWithInstallerMediaType() throws Exception {
        var apk = Files.createTempFile("home-ktv-tv", ".apk");
        Files.write(apk, new byte[]{1, 2, 3});
        MockMvc mvc = standaloneSetup(new ReleaseController(properties(apk.toUri().toString()), new DefaultResourceLoader())).build();

        mvc.perform(get("/api/release/tv/apk/arm64-v8a"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/vnd.android.package-archive")))
                .andExpect(header().string("Content-Disposition", containsString("home-ktv-tv-1.2.3-arm64-v8a.apk")))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    private AppProperties properties(String apkPath) {
        AppProperties properties = new AppProperties();
        properties.getRelease().setVersion("1.2.3");
        properties.getRelease().setVersionCode(123);
        properties.getRelease().setArmeabiV7aApk(apkPath);
        properties.getRelease().setArm64V8aApk(apkPath);
        return properties;
    }
}
