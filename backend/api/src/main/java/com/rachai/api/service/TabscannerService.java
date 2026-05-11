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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;

import com.rachai.api.dto.TabscannerResponseDTO;


@Service



public class TabscannerService {
     
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tabscanner.api.key}")
    private String apiKey;

    private final String API_URL = "https://api.tabscanner.com/api/2/process";


    public TabscannerResponseDTO processReceipt(MultipartFile file) throws IOException {
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
        body.add("nearWords", "true");
        body.add("documentType", "receipt");
        body.add("isTable", "true");
        

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        //  chamada POST
        ResponseEntity<TabscannerResponseDTO> response = restTemplate.postForEntity(
        API_URL, 
        requestEntity, 
        TabscannerResponseDTO.class // covnersao do JSON
        );

        return response.getBody();
    }

    public TabscannerResponseDTO searchResult(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String urlResult = "https://api.tabscanner.com/api/result/" + token;

        ResponseEntity<TabscannerResponseDTO> response = restTemplate.exchange(
            urlResult, 
            HttpMethod.GET, 
            entity, 
            TabscannerResponseDTO.class);

        TabscannerResponseDTO dto = response.getBody();

        return dto;
    }

    public String extractToken(TabscannerResponseDTO response) {
        if (response != null && response.getToken() != null){
            return response.getToken();
        }
        throw new RuntimeException("Token não encontrado");
    }
    
   

    public void filterResponse(TabscannerResponseDTO dto){
        if (dto.getResult() == null || dto.getResult().getLineItems() == null) return;

       dto.getResult().getLineItems().forEach(item -> {

        if (item.getQty() == null) {
            throw new NullPointerException("Quantidade não encontrada no item: " + item.getDesc());
        }


        // pode lançar uma exception, que o handler captura
        double valor = Double.parseDouble(item.getQty().toString());

        if (valor <= 0) {
            // personalizada
            throw new IllegalArgumentException("Quantidade inválida para o item: " + item.getDesc());
        }

        item.setQty(valor);
        });
    }
}
