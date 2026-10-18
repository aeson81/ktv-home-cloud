package com.homektv.config;

import com.homektv.library.MediaImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时自动扫描（由 KTV_AUTO_SCAN 环境变量控制）。
 * fpk 的「运行设置」里「启动时自动扫描歌曲库」对应此开关。
 */
@Component
public class AutoScanRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AutoScanRunner.class);

    private final MediaImportService mediaImportService;

    public AutoScanRunner(MediaImportService mediaImportService) {
        this.mediaImportService = mediaImportService;
    }

    @Override
    public void run(ApplicationArguments args) {
        String autoScan = System.getenv("KTV_AUTO_SCAN");
        if ("true".equalsIgnoreCase(autoScan)) {
            log.info("[AutoScan] KTV_AUTO_SCAN=true，启动后自动扫描曲库...");
            try {
                // 延迟 10 秒，等数据库和挂载就绪
                Thread.sleep(10000);
                MediaImportService.SourceScanResult result = mediaImportService.scanSourceLibrary();
                log.info("[AutoScan] 自动扫描完成: {}", result);
            } catch (Exception e) {
                log.error("[AutoScan] 自动扫描失败", e);
            }
        }
    }
}
