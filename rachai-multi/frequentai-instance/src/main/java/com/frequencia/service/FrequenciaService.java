package com.frequencia.service;

import com.frequencia.dto.EstatisticaAlunoDTO;
import com.frequencia.model.Student;
import com.frequencia.model.RegistroFrequencia;
import com.frequencia.model.Turma;
import com.framework.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service bem simples, guardando tudo em memória (sem banco), só para
 * deixar a instância fácil de rodar e testar. Reutiliza a exceção
 * ResourceNotFoundException que já vem do framework.
 */
@Service
public class FrequenciaService {

    /** Limite de faltas (%) a partir do qual o aluno é considerado em risco. */
    public static final double LIMITE_RISCO_PERCENTUAL = 25.0;

    private final Map<Long, Turma> turmas = new ConcurrentHashMap<>();
    private final Map<Long, RegistroFrequencia> registros = new ConcurrentHashMap<>();

    private final AtomicLong turmaIds = new AtomicLong(1);
    private final AtomicLong StudentIds = new AtomicLong(1);
    private final AtomicLong registroIds = new AtomicLong(1);

    public Turma criarTurma(String nome) {
        Turma turma = new Turma(turmaIds.getAndIncrement(), nome, new java.util.ArrayList<>());
        turmas.put(turma.getId(), turma);
        return turma;
    }

    public Student matricularStudent(Long turmaId, String nome, String email) {
        Turma turma = buscarTurma(turmaId);
        Student Student = new Student(StudentIds.getAndIncrement(), nome, email);
        turma.getStudents().add(Student);
        return Student;
    }

    public Turma buscarTurma(Long turmaId) {
        Turma turma = turmas.get(turmaId);
        if (turma == null) {
            throw new ResourceNotFoundException("Turma não encontrada: " + turmaId);
        }
        return turma;
    }

    public RegistroFrequencia registrarPresenca(Long turmaId, Long StudentId, boolean presente) {
        Turma turma = buscarTurma(turmaId);

        boolean StudentExiste = turma.getStudents().stream().anyMatch(a -> a.getId().equals(StudentId));
        if (!StudentExiste) {
            throw new ResourceNotFoundException("Student não encontrado na turma: " + StudentId);
        }

        RegistroFrequencia registro = new RegistroFrequencia(
                registroIds.getAndIncrement(), turmaId, StudentId, LocalDate.now(), presente);
        registros.put(registro.getId(), registro);
        return registro;
    }

    public List<RegistroFrequencia> listarPorTurma(Long turmaId) {
        return registros.values().stream()
                .filter(r -> r.getTurmaId().equals(turmaId))
                .collect(Collectors.toList());
    }

    public List<Turma> listarTurmas() {
        return List.copyOf(turmas.values());
    }

    /**
     * Calcula, no servidor, a estatística de assiduidade de cada aluno da
     * turma (total de registros, faltas, % de faltas e se está em risco).
     * Antes esse cálculo só existia solto no frontend; agora é uma regra de
     * negócio única e confiável, reaproveitada tanto pela tela quanto pela
     * análise de IA.
     */
    public List<EstatisticaAlunoDTO> calcularEstatisticas(Long turmaId) {
        Turma turma = buscarTurma(turmaId);
        List<RegistroFrequencia> registrosDaTurma = listarPorTurma(turmaId);

        return turma.getStudents().stream()
                .map(aluno -> {
                    List<RegistroFrequencia> registrosDoAluno = registrosDaTurma.stream()
                            .filter(r -> r.getStudentId().equals(aluno.getId()))
                            .collect(Collectors.toList());

                    long total = registrosDoAluno.size();
                    long faltas = registrosDoAluno.stream().filter(r -> !r.isPresente()).count();
                    double percentualFaltas = total > 0 ? (faltas * 100.0) / total : 0.0;
                    boolean emRisco = total > 0 && percentualFaltas > LIMITE_RISCO_PERCENTUAL;

                    return new EstatisticaAlunoDTO(
                            aluno.getId(), aluno.getName(), total, faltas, percentualFaltas, emRisco);
                })
                .collect(Collectors.toList());
    }
}
