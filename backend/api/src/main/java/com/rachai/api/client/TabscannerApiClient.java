package com.rachai.api.client;

import com.rachai.api.dto.TabscannerResponseDTO;
import com.rachai.api.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class TabscannerApiClient {

    @Value("${tabscanner.api.key}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    private static final String PROCESS_URL = "https://api.tabscanner.com/api/2/process";
    private static final String RESULT_URL = "https://api.tabscanner.com/api/result/";

    /**
     * Envia uma imagem de recibo para a Tabscanner iniciar o processamento.
     *
     * @param file arquivo de imagem do recibo
     * @return resposta inicial contendo o token para consulta posterior
     */
    public TabscannerResponseDTO processReceipt(MultipartFile file) {
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

            ResponseEntity<TabscannerResponseDTO> response = restTemplate.postForEntity(
                    PROCESS_URL,
                    requestEntity,
                    TabscannerResponseDTO.class);

            return response.getBody();

        } catch (IOException e) {
            throw new BusinessException("Erro ao ler o arquivo enviado: " + e.getMessage());
        }
    }

    /**
     * Consulta o resultado do processamento de um recibo usando o token retornado
     * pelo método {@link #processReceipt}.
     *
     * @param token identificador do processamento
     * @return resultado completo com os itens do recibo
     */
    public TabscannerResponseDTO getResult(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<TabscannerResponseDTO> response = restTemplate.exchange(
                RESULT_URL + token,
                HttpMethod.GET,
                entity,
                TabscannerResponseDTO.class);

        return response.getBody();
    }
}