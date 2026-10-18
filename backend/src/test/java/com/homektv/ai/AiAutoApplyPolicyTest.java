package com.homektv.ai;

import com.homektv.config.AppProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiAutoApplyPolicyTest {
    @Test
    void autoAppliesAtConfiguredNinetyPercentThreshold() {
        AppProperties properties = new AppProperties();
        properties.getAi().setAutoApplyConfidence(0.90);
        AiAutoApplyPolicy policy = new AiAutoApplyPolicy(properties);

        assertThat(policy.shouldAutoApply(result(0.90))).isTrue();
        assertThat(policy.shouldAutoApply(result(0.95))).isTrue();
        assertThat(policy.shouldAutoApply(result(0.899))).isFalse();
    }

    @Test
    void classificationClampsInvalidConfidenceValues() {
        assertThat(result(1.4).confidence()).isEqualTo(1.0);
        assertThat(result(-0.2).confidence()).isZero();
    }

    private AiSongClassification result(double confidence) {
        return new AiSongClassification("国语", "00年代", List.of("流行"), List.of("爱情"),
                "全年龄", "组合", List.of("华语流行"), "测试", confidence);
    }
}
