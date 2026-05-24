package com.rachai.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rachai.api.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Component
public class GroqApiClient {

    @Value("${groq.api.key:}")
    private String groqApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";

    /**
     * Envia um prompt para a API da Groq e retorna o texto da resposta.
     *
     * @param systemMessage instrução de sistema enviada ao modelo
     * @param userPrompt    conteúdo do prompt do usuário
     * @return texto gerado pelo modelo
     */
    public String chat(String systemMessage, String userPrompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", MODEL,
                    "max_tokens", 1024,
                    "temperature", 0.7,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemMessage),
                            Map.of("role", "user", "content", userPrompt))));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new BusinessException(
                        "Não foi possível gerar uma sugestão automática no momento. Tente novamente mais tarde.");
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("A requisição para o serviço de IA foi interrompida.");
        } catch (Exception e) {
            throw new BusinessException("Erro ao comunicar com o serviço de IA: " + e.getMessage());
        }
    }
}