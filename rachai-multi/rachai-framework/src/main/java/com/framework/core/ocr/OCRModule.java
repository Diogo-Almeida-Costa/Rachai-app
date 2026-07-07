package com.framework.core.ocr;

import com.framework.core.exception.BusinessException;
import com.framework.extension.ocr.IOCRExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.core.client.TabscannerApiClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.framework.extension.resource.IResource;

@Component
public class OCRModule {

    
    private static final Logger logger = LoggerFactory.getLogger(OCRModule.class);
    
    private final TabscannerApiClient tabscannerApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OCRModule(TabscannerApiClient tabscannerApiClient) {
        this.tabscannerApiClient = tabscannerApiClient;
    }
    
    public <T extends IResource> T processDocument(MultipartFile file, IOCRExtension<T> extension) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo enviado não pode estar vazio");
        }
        
        TabscannerResponseDTO response = tabscannerApiClient.processReceipt(file);
        
        if (response == null || response.getToken() == null) {
            throw new BusinessException("Falha ao iniciar processamento OCR");
        }
        
        return extension.parseAndValidate(response.toString());
    }
    
    public <T extends IResource> T processWithToken(String token, IOCRExtension<T> extension) {
        TabscannerResponseDTO response = tabscannerApiClient.getResult(token);
        return extension.parseAndValidate(response.toString());
    }
}
