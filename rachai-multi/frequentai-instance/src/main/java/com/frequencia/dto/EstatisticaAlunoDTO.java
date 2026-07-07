package com.frequencia.dto;

/**
 * Estatística de frequência de um aluno, calculada no backend (antes esse
 * cálculo só existia no frontend, o que é o "bug de lógica" reportado: a
 * regra de risco de faltas é uma regra de negócio do domínio e deve viver no
 * servidor, não ser recalculada de forma solta na tela).
 */
public record EstatisticaAlunoDTO(
        Long alunoId,
        String alunoNome,
        long totalRegistros,
        long faltas,
        double percentualFaltas,
        boolean emRisco
) {
}
