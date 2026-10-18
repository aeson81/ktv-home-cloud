package com.homektv.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查端点（P0.2 验收）。
 *
 * Health check endpoint (P0.2 acceptance).
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 返回服务健康状态，供 TV 端局域网扫描确认这是点歌服务端而非其它 HTTP 服务。
     *
     * Returns service health status for TV-side LAN scanning to confirm this is a karaoke server, not another HTTP service.
     *
     * @return 包含 {@code status} 和 {@code service} 标识的 Map
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        // service 标识供 TV 端局域网扫描确认「这是点歌服务端」而非其它 http 服务
        return Map.of("status", "UP", "service", "home-ktv");
    }
}
