package com.taskflow.service;

import com.framework.core.Framework;
import com.framework.core.context.ContextManager;
import com.framework.core.ocr.OCRModule;
import com.framework.core.splitter.ResourceSplitter;
import com.framework.extension.ai.IAIConfig;
import com.framework.extension.user.IUser;
import com.taskflow.model.Project;
import com.taskflow.ocr.TaskListOCRExtension;
import com.taskflow.rule.TaskDelegationSplitRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orquestra o fluxo completo do framework para o domínio de tarefas: OCR ->
 * IA contextual -> divisão/delegação de carga entre colaboradores. Segue
 * exatamente o mesmo roteiro usado pelo RachAIService (rachai-instance),
 * trocando apenas as extensões concretas utilizadas em cada etapa.
 */
@Service
public class TaskFlowFrameworkService {

    @Autowired
    private ContextManager contextManager;

    @Autowired
    private OCRModule ocrModule;

    @Autowired
    private ResourceSplitter resourceSplitter;

    @Autowired
    private Framework framework;

    @Autowired
    private TaskFlowService taskFlowService;

    public Map<IUser, BigDecimal> processarListaDeTarefas(MultipartFile file, List<Long> collaboratorIds) {
        // 1. OCR (Ponto Fixo: OCRModule + Extensão Flexível: TaskListOCRExtension)
        Project project = ocrModule.processDocument(file, new TaskListOCRExtension());

        // 2. IA Contextual (Ponto Fixo: ContextManager + Extensão Flexível: TaskAIConfig via registry)
        IAIConfig aiConfig = framework.getRegistry().get(IAIConfig.class);
        String prompt = contextManager.buildPrompt(aiConfig, project.getPendingTasks());
        // O prompt acima seria enviado a um LLM real; o ContextManager só orquestra a montagem.

        // 3. Colaboradores participantes do grupo/projeto
        List<IUser> participants = collaboratorIds.stream()
                .map(taskFlowService::buscarColaborador)
                .map(collaborator -> (IUser) collaborator)
                .collect(Collectors.toList());

        // 4. Delegação/Divisão de carga (Ponto Fixo: ResourceSplitter + Extensão Flexível: TaskDelegationSplitRule)
        return resourceSplitter.calculateSplit(project, participants, new TaskDelegationSplitRule());
    }
}
