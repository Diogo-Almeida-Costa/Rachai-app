package com.frequencia.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class RegistroFrequencia {
    private Long id;
    private Long turmaId;
    private Long studentId; // Relacionamento com o seu Student (que é o IUser da instância)
    private LocalDate data;
    private boolean presente;

    public RegistroFrequencia() {
    }

    public RegistroFrequencia(Long id, Long studentId, Long turmaId, LocalDate data, boolean presente) {
        this.id = id;
        this.studentId = studentId;
        this.turmaId = turmaId;
        this.data = data;
        this.presente = presente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getTurmaId(){ return turmaId;}
    public void setTurmaId(Long turmaId) { this.turmaId = turmaId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
   
    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public boolean isPresente() {
        return presente;
    }

    public void setPresente(boolean presente) {
        this.presente = presente;
    }
    
}
