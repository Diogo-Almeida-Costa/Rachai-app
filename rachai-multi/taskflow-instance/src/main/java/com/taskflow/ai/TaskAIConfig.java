package com.taskflow.ai;

import com.framework.extension.ai.IAIConfig;
import org.springframework.stereotype.Component;

/**
 * Implementação do ponto de extensão de IA do framework para o domínio de
 * Controle de Tarefas (equivalente à FrequenciaAIConfig/RachAIConfig).
 * Quem consome esta configuração (ContextManager, peça fixa do core) não
 * conhece esta classe diretamente, apenas a interface IAIConfig.
 */
@Component
public class TaskAIConfig implements IAIConfig {

    @Override
    public String getSystemInstructions() {
        return "Você é o assistente de IA do TaskFlow, uma plataforma de delegação inteligente de tarefas "
                + "entre colaboradores de um grupo ou projeto. Sua função é analisar as tarefas pendentes e "
                + "sugerir novas tarefas correlatas, além de uma recomendação preditiva sobre riscos de atraso "
                + "ou sobrecarga do grupo.";
    }

    @Override
    public String getDomainContext() {
        return "Domínio: delegação de tarefas em grupo (TaskFlow). Um projeto possui várias tarefas pendentes, "
                + "cada uma com título, prioridade (ALTA, MEDIA ou BAIXA) e esforço estimado. As tarefas são "
                + "delegadas entre os colaboradores do grupo de forma proporcional à prioridade das tarefas e "
                + "inversamente proporcional à ocupação atual de cada colaborador (colaboradores menos ocupados "
                + "recebem mais carga nova).";
    }

    @Override
    public String getResponseSchema() {
        return "{\n"
                + "  \"tarefasSugeridas\": [\n"
                + "    { \"titulo\": \"string\", \"prioridade\": \"ALTA|MEDIA|BAIXA\", \"motivo\": \"string\" }\n"
                + "  ],\n"
                + "  \"recomendacaoPreditiva\": \"string - observação sobre riscos de atraso ou sobrecarga\"\n"
                + "}";
    }
}
