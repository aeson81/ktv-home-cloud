package com.homektv.web;

import com.homektv.ai.AiConfigService;
import com.homektv.ai.OpenAiCompatibleClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai/config")
public class AiConfigController {
    private final AiConfigService configService;
    private final OpenAiCompatibleClient client;

    public AiConfigController(AiConfigService configService, OpenAiCompatibleClient client) {
        this.configService = configService;
        this.client = client;
    }

    @GetMapping
    public AiConfigService.ConfigResponse get() { return configService.response(); }

    @PutMapping
    public AiConfigService.ConfigResponse put(@RequestBody AiConfigService.ConfigUpdate request) {
        return configService.update(request);
    }

    @GetMapping("/models")
    public Map<String, Object> models() { return Map.of("models", client.listModels()); }

    @PostMapping("/test")
    public Map<String, Object> test() { return client.testConnection(); }
}
