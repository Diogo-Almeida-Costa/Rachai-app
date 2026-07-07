package com.frequencia.dto;

import java.util.List;

public record AnaliseRiscoDTO(
        List<AlunoRiscoDTO> alunosEmRisco,
        String observacaoGeral,
        String origem // "ia" (Groq respondeu) ou "fallback-local" (Groq indisponível)
) {
    public record AlunoRiscoDTO(Long alunoId, String nome, double percentualFaltas) {
    }
}
