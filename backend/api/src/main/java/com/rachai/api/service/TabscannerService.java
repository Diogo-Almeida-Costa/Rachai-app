package com.rachai.api.service;

import com.rachai.api.client.TabscannerApiClient;
import com.rachai.api.dto.TabscannerResponseDTO;
import com.rachai.api.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TabscannerService {

    @Autowired
    private TabscannerApiClient tabscannerApiClient;

    public TabscannerResponseDTO processReceipt(MultipartFile file) {
        return tabscannerApiClient.processReceipt(file);
    }

    public TabscannerResponseDTO searchResult(String token) {
        return tabscannerApiClient.getResult(token);
    }

    public String extractToken(TabscannerResponseDTO response) {
        if (response != null && response.getToken() != null) {
            return response.getToken();
        }
        throw new BusinessException("Token não encontrado");
    }

    public void filterResponse(TabscannerResponseDTO dto) {
        if (dto.getResult() == null || dto.getResult().getLineItems() == null) {
            throw new BusinessException("Resultado ou itens não encontrados");
        }

        dto.getResult().getLineItems().forEach(item -> {

            if (item.getQty() == null) {
                throw new BusinessException("Quantidade não encontrada no item: " + item.getDesc());
            }

            double valor = Double.parseDouble(item.getQty().toString());

            if (valor <= 0) {
                throw new BusinessException("Quantidade inválida para o item: " + item.getDesc());
            }

            item.setQty(valor);
        });
    }
}