package com.taskflow.dto;

import java.util.List;

public record SugestaoTarefasDTO(
        List<TarefaSugeridaDTO> tarefasSugeridas,
        String recomendacaoPreditiva,
        String origem // "ia" (Groq respondeu) ou "fallback-local" (Groq indisponível)
) {
    public record TarefaSugeridaDTO(String titulo, String prioridade, String motivo) {
    }
}
