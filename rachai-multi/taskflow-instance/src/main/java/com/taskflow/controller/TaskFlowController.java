package com.taskflow.controller;

import com.taskflow.model.Collaborator;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.service.TaskFlowAIService;
import com.taskflow.service.TaskFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/taskflow")
public class TaskFlowController {

    @Autowired
    private TaskFlowService taskFlowService;

    @Autowired
    private TaskFlowAIService taskFlowAIService;

    @PostMapping("/projetos")
    public Project criarProjeto(@RequestParam String nome) {
        return taskFlowService.criarProjeto(nome);
    }

    @GetMapping("/projetos")
    public List<Project> listarProjetos() {
        return taskFlowService.listarProjetos();
    }

    @PostMapping("/projetos/{projectId}/colaboradores")
    public Collaborator adicionarColaborador(@PathVariable Long projectId,
                                              @RequestParam String nome,
                                              @RequestParam String email,
                                              @RequestParam(defaultValue = "0") int ocupacaoInicial) {
        return taskFlowService.adicionarColaborador(projectId, nome, email, ocupacaoInicial);
    }

    @GetMapping("/projetos/{projectId}/colaboradores")
    public List<Collaborator> listarColaboradores(@PathVariable Long projectId) {
        return taskFlowService.listarColaboradoresDoProjeto(projectId);
    }

    @PostMapping("/projetos/{projectId}/tarefas")
    public Task criarTarefa(@PathVariable Long projectId,
                             @RequestParam String titulo,
                             @RequestParam Task.Priority prioridade,
                             @RequestParam BigDecimal esforcoEstimado) {
        return taskFlowService.criarTarefa(projectId, titulo, prioridade, esforcoEstimado);
    }

    @GetMapping("/projetos/{projectId}/tarefas")
    public List<Task> listarTarefasPendentes(@PathVariable Long projectId) {
        return taskFlowService.listarTarefasPendentes(projectId);
    }

    @PostMapping("/projetos/{projectId}/tarefas/{taskId}/concluir")
    public Task concluirTarefa(@PathVariable Long projectId, @PathVariable Long taskId) {
        return taskFlowService.concluirTarefa(projectId, taskId);
    }

    @GetMapping("/projetos/{projectId}/prompt-sugestao")
    public String gerarPromptDeSugestao(@PathVariable Long projectId) {
        List<Task> pendentes = taskFlowService.listarTarefasPendentes(projectId);
        return taskFlowAIService.gerarPromptDeSugestao(pendentes);
    }

    /** Chama de fato a IA (Groq) e retorna a sugestão de tarefas já interpretada, com fallback local. */
    @GetMapping("/projetos/{projectId}/analise-sugestao")
    public com.taskflow.dto.SugestaoTarefasDTO analisarSugestao(@PathVariable Long projectId) {
        List<Task> pendentes = taskFlowService.listarTarefasPendentes(projectId);
        return taskFlowAIService.gerarSugestao(pendentes);
    }
}
