package com.taskflow.model;

import com.framework.extension.user.IUser;

/**
 * Colaborador de um projeto/grupo. É a implementação concreta desta instância
 * para o contrato IUser do framework (mesmo papel que Student cumpre na
 * instância de Controle de Frequência).
 *
 * occupationLevel representa o quão ocupado o colaborador já está (0 = livre,
 * 100 = totalmente ocupado). É usado pela regra de divisão para decidir quanto
 * de carga nova cada um deve receber.
 */
public class Collaborator implements IUser {
    private Long id;
    private String name;
    private String email;
    private int occupationLevel;

    public Collaborator() {
    }

    public Collaborator(Long id, String name, String email, int occupationLevel) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.occupationLevel = occupationLevel;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    public int getOccupationLevel() {
        return this.occupationLevel;
    }

    // Setters manuais, seguindo o mesmo padrão adotado em Student (frequentai-instance)
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOccupationLevel(int occupationLevel) {
        this.occupationLevel = occupationLevel;
    }
}
