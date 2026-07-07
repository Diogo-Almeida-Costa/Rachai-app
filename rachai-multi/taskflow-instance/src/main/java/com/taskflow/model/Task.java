package com.taskflow.model;

import java.math.BigDecimal;

/**
 * Tarefa individual dentro de um Project. É específica desta instância, sem
 * relação direta com o framework (assim como RegistroFrequencia é específico
 * da instância de Controle de Frequência).
 */
public class Task {

    public enum Priority {
        ALTA(3), MEDIA(2), BAIXA(1);

        private final int weight;

        Priority(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return this.weight;
        }
    }

    private Long id;
    private Long projectId;
    private String title;
    private Priority priority;
    private BigDecimal estimatedEffort; // esforço estimado (ex: horas ou pontos)
    private Long assignedToId; // preenchido após a delegação
    private boolean concluded;

    public Task() {
    }

    public Task(Long id, Long projectId, String title, Priority priority, BigDecimal estimatedEffort) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.priority = priority;
        this.estimatedEffort = estimatedEffort;
        this.concluded = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public BigDecimal getEstimatedEffort() {
        return estimatedEffort;
    }

    public void setEstimatedEffort(BigDecimal estimatedEffort) {
        this.estimatedEffort = estimatedEffort;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public boolean isConcluded() {
        return concluded;
    }

    public void setConcluded(boolean concluded) {
        this.concluded = concluded;
    }
}
