package com.rachai.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;



@Service



public class TabscannerService {
     
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tabscanner.api.key}")
    private String apiKey;

    private final String API_URL = "https://api.tabscanner.com/api/2/process";

    public String processReceipt(MultipartFile file) throws IOException {
        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("apikey", apiKey);

        // prepara o arquivo para o envio
        // a api ext espera o arquivo no campo "file"
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // recurso temp para o arquivo que veio do upload
        ByteArrayResource contentsAsResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        
        body.add("file", contentsAsResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        //  chamada POST
        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, requestEntity, String.class);


        System.out.println("Resposta Tabscanner: " + response.getBody());
        return response.getBody();
    }

    public String searchResult(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String urlResult = "https://api.tabscanner.com/api/result/" + token;

        ResponseEntity<String> response = restTemplate.exchange(
            urlResult, 
            HttpMethod.GET, 
            entity, 
            String.class);
        return response.getBody();
    }

    public String extractToken(String jsonResponse) {
        try {
            
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            if (root.has("token")) {
                return root.get("token").asText();
            }
            throw new RuntimeException("Token não encontrado na resposta do Tabscanner");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar JSON: " + e.getMessage());
        }
    }
}
