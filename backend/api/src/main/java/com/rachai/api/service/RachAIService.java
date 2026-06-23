package com.rachai.api.service;

import com.rachai.framework.core.context.ContextManager;
import com.rachai.framework.core.ocr.OCRModule;
import com.rachai.framework.core.splitter.ResourceSplitter;
import com.rachai.framework.core.user.User;
import com.rachai.api.instance.ai.RachAIConfig;
import com.rachai.api.instance.ocr.ReceiptOCRExtension;
import com.rachai.api.instance.resource.ExpenseResource;
import com.rachai.api.instance.rule.EqualSplitRule;
import com.rachai.api.model.Group;
import com.rachai.api.repository.GroupRepository;
import com.rachai.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RachAIService {

    @Autowired
    private ContextManager contextManager;

    @Autowired
    private OCRModule ocrModule;

    @Autowired
    private ResourceSplitter resourceSplitter;

    @Autowired
    private RachAIConfig aiConfig;

    @Autowired
    private GroupRepository groupRepository;

    public Map<User, BigDecimal> processExpenseWithFramework(MultipartFile file, Long groupId) {
        // 1. OCR (Ponto Fixo + Extensão Flexível)
        ExpenseResource resource = ocrModule.processDocument(file, new ReceiptOCRExtension());

        // 2. IA Contextual (Ponto Fixo + Extensão Flexível)
        String prompt = contextManager.buildPrompt(aiConfig, resource);
        // Aqui o prompt seria enviado para um LLM real. No framework, o ContextManager orquestra isso.

        // 3. Obter participantes do grupo real
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));
        
        List<User> participants = group.getMembers().stream()
                .map(User::fromModel)
                .collect(Collectors.toList());

        // 4. Divisão de Recurso (Ponto Fixo + Extensão Flexível)
        return resourceSplitter.calculateSplit(resource, participants, new EqualSplitRule());
    }
}
