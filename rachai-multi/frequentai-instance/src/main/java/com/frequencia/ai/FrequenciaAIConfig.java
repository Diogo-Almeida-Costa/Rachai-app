package com.frequencia.ai;

import com.framework.extension.ai.IAIConfig;
import org.springframework.stereotype.Component;

/**
 * Implementação do "ponto de extensão" de IA do framework.
 * Quem usa essa configuração (ContextManager) não conhece a classe
 * FrequenciaAIConfig diretamente, só a interface IAIConfig.
 */
@Component
public class FrequenciaAIConfig implements IAIConfig {

    @Override
    public String getSystemInstructions() {
        return "Você é um assistente que analisa frequência escolar e aponta alunos em risco por excesso de faltas.";
    }

    @Override
    public String getDomainContext() {
        return "O sistema registra presença/falta de alunos por turma e data. " +
                "Considere risco quando o aluno ultrapassa 25% de faltas no período.";
    }

    @Override
    public String getResponseSchema() {
        return "{ \"alunosEmRisco\": [ { \"alunoId\": \"number\", \"percentualFaltas\": \"number\" } ] }";
    }
}
