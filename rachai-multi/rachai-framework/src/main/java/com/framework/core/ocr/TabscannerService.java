package com.framework.core.ocr;

import com.framework.core.client.TabscannerApiClient;
import com.framework.core.ocr.TabscannerResponseDTO;

import com.framework.core.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Service
public class TabscannerService {

    private static final Logger logger = LoggerFactory.getLogger(TabscannerService.class);

    @Autowired
    private TabscannerApiClient tabscannerApiClient;

    public TabscannerService(TabscannerApiClient tabscannerApiClient) {
        this.tabscannerApiClient = tabscannerApiClient;
    }

    public TabscannerResponseDTO processReceipt(MultipartFile file) {
        logger.info("Attempting to upload and process receipt image via Tabscanner API");
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Uploaded file cannot be null or empty");
        }
        return tabscannerApiClient.processReceipt(file);
    }

    

    public TabscannerResponseDTO searchResult(String token) {
        logger.info("Polling Tabscanner OCR result for token: {}", token);
        if (token == null || token.isBlank()) {
            throw new BusinessException("Token cannot be null or empty");
        }
        return tabscannerApiClient.getResult(token);
    }

    public String extractToken(TabscannerResponseDTO response) {
        if (response != null && response.getToken() != null) {
            return response.getToken();
        }
        logger.error("Failed to extract token from Tabscanner response");
        throw new BusinessException("Token não encontrado na resposta do scanner");
    }

    public void filterResponse(TabscannerResponseDTO dto) {
        logger.info("Filtering and validating parsed line items from Tabscanner result");

        if (dto == null || dto.getResult() == null || dto.getResult().getLineItems() == null) {
            throw new BusinessException("Resultado ou itens não encontrados no cupom fiscal");
        }

        dto.getResult().getLineItems().forEach(item -> {
            String itemDescription = (item.getDesc() != null) ? item.getDesc() : "Item Desconhecido";

            if (item.getQty() == null) {
                throw new BusinessException("Quantidade não encontrada no item: " + itemDescription);
            }

            try {
                BigDecimal quantity = new BigDecimal(item.getQty().toString());

                if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("Quantidade inválida para o item: " + itemDescription);
                }

                item.setQty(quantity);

            } catch (NumberFormatException e) {
                logger.error("Failed to parse quantity '{}' for item '{}'", item.getQty(), itemDescription);
                throw new BusinessException("Erro de formato na quantidade do item: " + itemDescription);
            }
        });
        
        logger.info("Successfully validated {} items from receipt", dto.getResult().getLineItems().size());
    }
}