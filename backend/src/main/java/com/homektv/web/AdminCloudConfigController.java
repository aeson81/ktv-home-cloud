package com.homektv.web;

import com.homektv.cloud.CloudConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 网盘配置管理：前端直接修改 115 Cookie / OneDrive 配置，保存后自动重新挂载。
 */
@RestController
@RequestMapping("/api/admin/cloud/config")
public class AdminCloudConfigController {

    private final CloudConfigService cloudConfigService;

    public AdminCloudConfigController(CloudConfigService cloudConfigService) {
        this.cloudConfigService = cloudConfigService;
    }

    @GetMapping
    public Map<String, Object> get() {
        return cloudConfigService.getConfig();
    }

    @PutMapping
    public Map<String, Object> update(@RequestBody Map<String, String> body) {
        return cloudConfigService.updateConfig(
                body.get("cookie115"),
                body.get("onedriveRegion"),
                body.get("onedriveToken"));
    }

    @PostMapping("/remount")
    public Map<String, Object> remount() {
        return Map.of("ok", true, "result", cloudConfigService.remount());
    }
}
