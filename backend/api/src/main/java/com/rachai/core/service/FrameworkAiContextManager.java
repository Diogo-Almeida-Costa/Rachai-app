package com.rachai.core.service;

import com.rachai.core.ai.LlmClient;
import org.springframework.stereotype.Service;

/**
 * Ponto Fixo: Gerencia o fluxo de contexto enviado à IA, aplicando as
 * restrições rígidas.
 */
@Service
public class FrameworkAiContextManager {

    private final LlmClient activeLlmClient;

    public FrameworkAiContextManager(LlmClient activeLlmClient) {
        this.activeLlmClient = activeLlmClient;
    }

    public String obtainSmartRecommendation(String strictDomainRules, String currentContext,
            String expectedJsonSchema) {
        // Ponto Fixo: Injeção obrigatória de restrições para garantir conformidade do
        // domínio
        String systemInstruction = "Você opera sob as regras fixas deste ecossistema: " + strictDomainRules
                + ". Responda obrigatoriamente em formato JSON estruturado conforme o seguinte Schema: "
                + expectedJsonSchema;

        // Delega a transmissão do payload ao cliente flexível ativo
        return activeLlmClient.invokeModel(systemInstruction, currentContext);
    }
}