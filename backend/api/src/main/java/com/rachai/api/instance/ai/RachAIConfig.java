package com.rachai.api.instance.ai;

import com.rachai.framework.extension.ai.IAIConfig;
import org.springframework.stereotype.Component;

@Component
public class RachAIConfig implements IAIConfig {
    @Override
    public String getSystemInstructions() {
        return "Você é o assistente do RachAI, especialista em organizar divisões de despesas e sugerir grupos.";
    }

    @Override
    public String getDomainContext() {
        return "O RachAI ajuda amigos a dividirem contas de forma justa, analisando o histórico de gastos e amizades.";
    }

    @Override
    public String getResponseSchema() {
        return "{ \"suggestion\": \"string\", \"confidence\": \"number\" }";
    }
}
