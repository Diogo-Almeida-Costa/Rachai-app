package com.taskflow.model;

import com.framework.extension.resource.IResource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Project é o "recurso a ser dividido" desta instância (equivalente à Expense
 * em rachai-instance). O valor total (getTotalValue) representa a carga de
 * trabalho pendente do projeto: a soma do esforço estimado de cada tarefa
 * pendente, ponderado pela sua prioridade. É esse valor que o
 * ResourceSplitter (peça fixa do core) vai delegar entre os colaboradores.
 */
public class Project implements IResource<BigDecimal> {

    private Long id;
    private String name;
    private List<Task> tasks = new ArrayList<>();
    private BigDecimal totalValue = BigDecimal.ZERO;

    public Project() {
    }

    public Project(Long id, String name) {
        this.id = id;
        this.name = name;
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
    public BigDecimal getTotalValue() {
        return this.totalValue;
    }

    @Override
    public void setTotalValue(BigDecimal value) {
        this.totalValue = value;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Task> getPendingTasks() {
        return tasks.stream().filter(t -> !t.isConcluded()).toList();
    }

    public void addTask(Task task) {
        if (task != null) {
            this.tasks.add(task);
            recalcularCargaTotal();
        }
    }

    /**
     * Recalcula a carga total (totalValue) somando esforço x peso de
     * prioridade de todas as tarefas ainda pendentes. Chamado sempre que uma
     * tarefa é adicionada ou concluída, para manter o recurso consistente
     * antes de ser passado ao ResourceSplitter.
     */
    public void recalcularCargaTotal() {
        BigDecimal total = getPendingTasks().stream()
                .map(t -> t.getEstimatedEffort().multiply(BigDecimal.valueOf(t.getPriority().getWeight())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        this.totalValue = total;
    }
}
