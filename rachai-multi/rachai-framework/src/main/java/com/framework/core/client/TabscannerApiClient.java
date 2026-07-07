package com.framework.core.client;

import com.framework.core.ocr.TabscannerResponseDTO;
import com.framework.core.exception.BusinessException;


import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;



@Component
public class TabscannerApiClient {

    private static final Logger logger = LoggerFactory.getLogger(TabscannerApiClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tabscanner.api.key}")
    private String apiKey;

    private static final String PROCESS_URL = "https://api.tabscanner.com/api/2/process";
    private static final String RESULT_URL = "https://api.tabscanner.com/api/result/";

    public TabscannerResponseDTO processReceipt(MultipartFile file) {
        logger.debug("Enviando requisição de processamento para Tabscanner: {}", PROCESS_URL);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("apikey", apiKey);

            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            body.add("nearWords", "true");
            body.add("documentType", "receipt");
            body.add("isTable", "true");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<TabscannerResponseDTO> response = restTemplate.postForEntity(PROCESS_URL, requestEntity, TabscannerResponseDTO.class);

            return response.getBody();

        } catch (IOException e) {
            logger.error("Erro na comunicação HTTP com Tabscanner (Process)", e);
            throw new BusinessException("Falha ao ler arquivo enviado: " + e.getMessage());
        }
    }

    public TabscannerResponseDTO getResult(String token) {
        logger.debug("Consultando resultado no Tabscanner para o token: {}", token);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);


            ResponseEntity<TabscannerResponseDTO> response = restTemplate.exchange(RESULT_URL + token, 
                HttpMethod.GET, 
                entity, 
                TabscannerResponseDTO.class);

            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Erro na comunicação HTTP com Tabscanner para o token: {}", token, e);
            throw new BusinessException("Falha ao extrair resultado: " + e.getMessage());
        }
        
    }
}