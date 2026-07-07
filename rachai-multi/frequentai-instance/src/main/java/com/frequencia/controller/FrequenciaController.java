package com.frequencia.controller;

import com.frequencia.dto.AnaliseRiscoDTO;
import com.frequencia.dto.EstatisticaAlunoDTO;
import com.frequencia.model.Student;
import com.frequencia.model.RegistroFrequencia;
import com.frequencia.model.Turma;
import com.frequencia.service.FrequenciaAIService;
import com.frequencia.service.FrequenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/frequencia")
public class FrequenciaController {

    @Autowired
    private FrequenciaService frequenciaService;

    @Autowired
    private FrequenciaAIService frequenciaAIService;

    @PostMapping("/turmas")
    public Turma criarTurma(@RequestParam String nome) {
        return frequenciaService.criarTurma(nome);
    }

    @GetMapping("/turmas")
    public List<Turma> listarTurmas() {
        return frequenciaService.listarTurmas();
    }

    @PostMapping("/turmas/{turmaId}/alunos")
    public Student matricularAluno(@PathVariable Long turmaId,
                                  @RequestParam String nome,
                                  @RequestParam String email) {
        return frequenciaService.matricularStudent(turmaId, nome, email);
    }

    @PostMapping("/turmas/{turmaId}/registros")
    public RegistroFrequencia registrarPresenca(@PathVariable Long turmaId,
                                                 @RequestParam Long alunoId,
                                                 @RequestParam boolean presente) {
        return frequenciaService.registrarPresenca(turmaId, alunoId, presente);
    }

    @GetMapping("/turmas/{turmaId}/registros")
    public List<RegistroFrequencia> listarRegistros(@PathVariable Long turmaId) {
        return frequenciaService.listarPorTurma(turmaId);
    }

    /** Estatísticas de assiduidade por aluno, calculadas no servidor (ponto fixo de regra de negócio). */
    @GetMapping("/turmas/{turmaId}/estatisticas")
    public List<EstatisticaAlunoDTO> calcularEstatisticas(@PathVariable Long turmaId) {
        return frequenciaService.calcularEstatisticas(turmaId);
    }

    /** Prompt puro montado pelo ContextManager, mantido apenas para fins de transparência/depuração. */
    @GetMapping("/turmas/{turmaId}/prompt-risco")
    public String gerarPromptDeRisco(@PathVariable Long turmaId) {
        List<EstatisticaAlunoDTO> estatisticas = frequenciaService.calcularEstatisticas(turmaId);
        return frequenciaAIService.gerarPromptDeRisco(estatisticas);
    }

    /** Chama de fato a IA (Groq) e retorna a análise de risco já interpretada, com fallback local. */
    @GetMapping("/turmas/{turmaId}/analise-risco")
    public AnaliseRiscoDTO analisarRisco(@PathVariable Long turmaId) {
        List<EstatisticaAlunoDTO> estatisticas = frequenciaService.calcularEstatisticas(turmaId);
        return frequenciaAIService.analisarRisco(estatisticas);
    }
}
