package com.frequencia.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Turma é específico desta instância, não tem relação com o framework.
 */

public class Turma {
    private Long id;
    private String nome;
    private List<Student> alunos = new ArrayList<>();

    public Turma() {
    }

    // 2. Construtor com todos os argumentos (AllArgsConstructor)
    public Turma(Long id, String nome, List<Student> alunos) {
        this.id = id;
        this.nome = nome;
        this.alunos = alunos != null ? alunos : new ArrayList<>();
    }

    // ==========================================
    // GETTERS E SETTERS MANUAIS (Substituindo o @Data)
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Student> getStudents() {
        return alunos;
    }

    public void setAlunos(List<Student> alunos) {
        this.alunos = alunos != null ? alunos : new ArrayList<>();
    }

    // ==========================================
    // MÉTODOS AUXILIARES ÚTEIS (Padrão para coleções)
    // ==========================================

    public void adicionarAluno(Student aluno) {
        if (this.alunos == null) {
            this.alunos = new ArrayList<>();
        }
        if (aluno != null) {
            this.alunos.add(aluno);
        }
    }    
}
