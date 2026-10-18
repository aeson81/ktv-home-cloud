package com.homektv.ai;

import org.springframework.stereotype.Component;

@Component
public class AiConcurrencyLimiter {
    private final AiConfigService configService;
    private int bulkActive;
    private int reasoningActive;

    public AiConcurrencyLimiter(AiConfigService configService) {
        this.configService = configService;
    }

    public Permit acquire(String role) {
        boolean reasoning = "REASONING".equals(role);
        synchronized (this) {
            while (active(reasoning) >= limit(reasoning)) {
                try {
                    wait();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new OpenAiCompatibleClient.AiProviderException(
                            "AI_INTERRUPTED", "AI 任务等待并发额度时被中断", exception);
                }
            }
            if (reasoning) reasoningActive++; else bulkActive++;
        }
        return new Permit(this, reasoning);
    }

    private synchronized void release(boolean reasoning) {
        if (reasoning) reasoningActive = Math.max(0, reasoningActive - 1);
        else bulkActive = Math.max(0, bulkActive - 1);
        notifyAll();
    }

    private int active(boolean reasoning) { return reasoning ? reasoningActive : bulkActive; }

    private int limit(boolean reasoning) {
        AiConfigService.ResolvedConfig config = configService.resolve();
        return reasoning ? config.reasoningConcurrency() : config.bulkConcurrency();
    }

    public static final class Permit implements AutoCloseable {
        private final AiConcurrencyLimiter owner;
        private final boolean reasoning;
        private boolean closed;

        private Permit(AiConcurrencyLimiter owner, boolean reasoning) {
            this.owner = owner;
            this.reasoning = reasoning;
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                owner.release(reasoning);
            }
        }
    }
}
