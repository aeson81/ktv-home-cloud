package com.homektv.discovery;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class LanDiscoveryServiceTest {
    @Test
    void responseContainsActualHttpPortAndEscapedName() {
        String payload = new String(LanDiscoveryService.responsePayload(12345, "客厅\"KTV"), StandardCharsets.UTF_8);

        assertThat(payload).contains("\"service\":\"home-ktv\"");
        assertThat(payload).contains("\"protocolVersion\":1");
        assertThat(payload).contains("\"port\":12345");
        assertThat(payload).contains("客厅\\\"KTV");
    }
}
