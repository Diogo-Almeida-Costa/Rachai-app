package com.framework.core.context;

import com.framework.extension.ai.IAIConfig;
import org.springframework.stereotype.Component;

@Component
public class ContextManager {
    
    public String buildPrompt(IAIConfig config, Object contextData) {
        StringBuilder sb = new StringBuilder();
        sb.append(config.getSystemInstructions()).append("\n\n");
        sb.append("=== CONTEXTO DO DOMÍNIO ===\n");
        sb.append(config.getDomainContext()).append("\n\n");
        sb.append("=== DADOS DE ENTRADA ===\n");
        sb.append(contextData.toString()).append("\n\n");
        sb.append("=== FORMATO DE RESPOSTA (JSON SCHEMA) ===\n");
        sb.append(config.getResponseSchema()).append("\n");
        return sb.toString();
    }
}
