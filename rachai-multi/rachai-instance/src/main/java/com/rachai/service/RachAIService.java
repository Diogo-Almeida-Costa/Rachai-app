package com.rachai.service;



import com.framework.core.context.ContextManager;
import com.framework.core.ocr.OCRModule;
import com.framework.core.splitter.ResourceSplitter;
import com.framework.core.user.User; // O User fixo do framework que criamos no passo anterior!
import com.framework.extension.user.IUser;

import com.rachai.ai.RachAIConfig;
import com.rachai.ocr.ReceiptOCRExtension;
import com.rachai.model.Expense;
import com.rachai.model.Group;
import com.rachai.rule.SplitRule;
import com.rachai.repository.GroupRepository;
import com.framework.core.exception.ResourceNotFoundException;
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

    public Map<IUser, BigDecimal> processExpenseWithFramework(MultipartFile file, Long groupId) {
        // 1. OCR (Ponto Fixo + Extensão Flexível)
        Expense resource = ocrModule.processDocument(file, new ReceiptOCRExtension());

        // 2. IA Contextual (Ponto Fixo + Extensão Flexível)
        String prompt = contextManager.buildPrompt(aiConfig, resource);
        // Aqui o prompt seria enviado para um LLM real. No framework, o ContextManager orquestra isso.

        // 3. Obter participantes do grupo real
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));
        
        List<IUser> participants = group.getMembers().stream()
                .map(member -> (IUser) User.fromModel(member))
                .collect(Collectors.toList());

        // 4. Divisão de Recurso (Ponto Fixo + Extensão Flexível)
        return resourceSplitter.calculateSplit(resource, participants, new SplitRule());
    }
}
