package com.rachai.framework.core.ocr;

import com.rachai.framework.extension.ocr.IOCRExtension;
import com.rachai.api.client.TabscannerApiClient;
import com.rachai.api.dto.TabscannerResponseDTO;
import com.rachai.api.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class OCRModule {
    
    @Autowired
    private TabscannerApiClient tabscannerApiClient;
    
    public <T> T processDocument(MultipartFile file, IOCRExtension<T> extension) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo enviado não pode estar vazio");
        }
        
        TabscannerResponseDTO response = tabscannerApiClient.processReceipt(file);
        
        if (response == null || response.getToken() == null) {
            throw new BusinessException("Falha ao iniciar processamento OCR");
        }
        
        return extension.parseAndValidate(response.toString());
    }
    
    public <T> T processWithToken(String token, IOCRExtension<T> extension) {
        TabscannerResponseDTO response = tabscannerApiClient.getResult(token);
        return extension.parseAndValidate(response.toString());
    }
}
