package com.framework.core.client;
 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.core.exception.BusinessException;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
 
import java.util.List;
import java.util.Map;
 
/**
 * Cliente genérico de infraestrutura para a API de chat da Groq (compatível com o
 * padrão OpenAI Chat Completions). É reutilizável por qualquer instanciação do
 * framework que precise enviar um prompt textual para um LLM e receber texto de volta.
 */
@Component
public class GroqApiClient {
 
    private static final Logger logger = LoggerFactory.getLogger(GroqApiClient.class);
 
    private static final String CHAT_URL = "https://api.groq.com/openai/v1/chat/completions";
 
    @Value("${groq.api.key}")
    private String apiKey;
 
    @Value("${groq.api.model:llama-3.3-70b-versatile}")
    private String model;
 
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
 
    /**
     * Envia uma mensagem de sistema e uma mensagem de usuário para a Groq e retorna
     * apenas o texto da resposta do modelo.
     */
    public String chat(String systemMessage, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("Chave de API da Groq não configurada (groq.api.key)");
        }
 
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
 
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemMessage),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "temperature", 0.4
            );
 
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
 
            ResponseEntity<String> response = restTemplate.exchange(
                    CHAT_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
 
            return extractContent(response.getBody());
 
        } catch (RestClientException e) {
            logger.error("Erro na comunicação HTTP com a Groq API", e);
            throw new BusinessException("Falha ao consultar a IA (Groq): " + e.getMessage());
        }
    }
 
    private String extractContent(String rawResponseBody) {
        try {
            JsonNode root = objectMapper.readTree(rawResponseBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
 
            if (content.isMissingNode() || content.isNull()) {
                throw new BusinessException("Resposta da Groq API não contém conteúdo válido");
            }
 
            return content.asText();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Falha ao interpretar resposta da Groq API: {}", rawResponseBody, e);
            throw new BusinessException("Falha ao interpretar resposta da IA (Groq)");
        }
    }
}
 