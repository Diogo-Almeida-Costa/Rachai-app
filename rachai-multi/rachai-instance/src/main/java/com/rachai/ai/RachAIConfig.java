package com.rachai.ai;

import com.framework.extension.ai.IAIConfig;
import com.rachai.service.AiGroupSuggestionService; 
import org.springframework.stereotype.Component;

@Component
public class RachAIConfig implements IAIConfig {
    // 1. Injeta o serviço antigo que tem a lógica pronta que você quer rodar
    private final AiGroupSuggestionService aiGroupSuggestionService;

    public RachAIConfig(AiGroupSuggestionService aiGroupSuggestionService) {
        this.aiGroupSuggestionService = aiGroupSuggestionService;
    }

    @Override
    public String getSystemInstructions() {
        return "Você é o assistente de IA do RachAI, uma plataforma de divisão de despesas "
                + "entre amigos e colegas. Sua função é analisar os dados de uma despesa "
                + "(possivelmente extraída de um cupom fiscal via OCR) e ajudar a interpretá-la "
                + "corretamente antes da divisão entre os participantes do grupo.";
    }

    @Override
    public String getDomainContext() {
        return "Domínio: divisão de despesas compartilhadas (RachAI). Uma despesa possui "
                + "descrição, valor total e é dividida entre os membros de um grupo. "
                + "A regra de divisão padrão é igualitária entre os participantes.";
    }

    @Override
    public String getResponseSchema() {
        return "{\n"
                + "  \"description\": \"string - descrição sugerida para a despesa\",\n"
                + "  \"amount\": \"number - valor total identificado\",\n"
                + "  \"notes\": \"string - observações relevantes, se houver\"\n"
                + "}";
    }
}
