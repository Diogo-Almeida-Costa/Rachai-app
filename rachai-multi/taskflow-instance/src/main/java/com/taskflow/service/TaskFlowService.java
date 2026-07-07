package com.taskflow.service;

import com.framework.core.exception.ResourceNotFoundException;
import com.taskflow.model.Collaborator;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service simples, guardando tudo em memória (sem banco), seguindo o mesmo
 * padrão adotado em FrequenciaService: deixa a instância fácil de rodar e
 * testar. Reutiliza a exceção ResourceNotFoundException que já vem do
 * framework.
 */
@Service
public class TaskFlowService {

    private final Map<Long, Project> projects = new ConcurrentHashMap<>();
    private final Map<Long, Collaborator> collaborators = new ConcurrentHashMap<>();
    // Associação colaborador <-> projeto (Collaborator é um IUser "puro" e não
    // carrega essa referência, então o vínculo é mantido aqui no service).
    private final Map<Long, List<Long>> colaboradorIdsPorProjeto = new ConcurrentHashMap<>();

    private final AtomicLong projectIds = new AtomicLong(1);
    private final AtomicLong collaboratorIds = new AtomicLong(1);
    private final AtomicLong taskIds = new AtomicLong(1);

    public Project criarProjeto(String nome) {
        Project project = new Project(projectIds.getAndIncrement(), nome);
        projects.put(project.getId(), project);
        return project;
    }

    public List<Project> listarProjetos() {
        return List.copyOf(projects.values());
    }

    public Project buscarProjeto(Long projectId) {
        Project project = projects.get(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("Projeto não encontrado: " + projectId);
        }
        return project;
    }

    public Collaborator adicionarColaborador(Long projectId, String nome, String email, int ocupacaoInicial) {
        buscarProjeto(projectId); // garante que o projeto existe
        Collaborator collaborator = new Collaborator(collaboratorIds.getAndIncrement(), nome, email, ocupacaoInicial);
        collaborators.put(collaborator.getId(), collaborator);
        colaboradorIdsPorProjeto
                .computeIfAbsent(projectId, k -> new CopyOnWriteArrayList<>())
                .add(collaborator.getId());
        return collaborator;
    }

    public List<Collaborator> listarColaboradores() {
        return List.copyOf(collaborators.values());
    }

    public List<Collaborator> listarColaboradoresDoProjeto(Long projectId) {
        buscarProjeto(projectId); // garante que o projeto existe
        List<Long> ids = colaboradorIdsPorProjeto.getOrDefault(projectId, new ArrayList<>());
        return ids.stream()
                .map(collaborators::get)
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }

    public Collaborator buscarColaborador(Long collaboratorId) {
        Collaborator collaborator = collaborators.get(collaboratorId);
        if (collaborator == null) {
            throw new ResourceNotFoundException("Colaborador não encontrado: " + collaboratorId);
        }
        return collaborator;
    }

    public Task criarTarefa(Long projectId, String titulo, Task.Priority prioridade, BigDecimal esforcoEstimado) {
        Project project = buscarProjeto(projectId);
        Task task = new Task(taskIds.getAndIncrement(), projectId, titulo, prioridade, esforcoEstimado);
        project.addTask(task);
        return task;
    }

    public List<Task> listarTarefasPendentes(Long projectId) {
        return buscarProjeto(projectId).getPendingTasks();
    }

    public Task concluirTarefa(Long projectId, Long taskId) {
        Project project = buscarProjeto(projectId);
        Task task = project.getTasks().stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + taskId));
        task.setConcluded(true);
        project.recalcularCargaTotal();
        return task;
    }
}
