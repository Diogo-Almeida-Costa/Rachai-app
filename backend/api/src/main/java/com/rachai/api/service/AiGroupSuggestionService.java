package com.rachai.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rachai.api.dto.GroupSuggestionDTO;
import com.rachai.api.model.Expense;
import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.repository.ExpenseRepository;
import com.rachai.api.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiGroupSuggestionService {

    
    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    //Gera uma sugestão de grupo com IA com base nos grupos e amigos do usuário.
    public GroupSuggestionDTO suggestGroup(User currentUser, List<Group> existingGroups, String context) {
        //Busca todos os amigos de um usuário
        List<User> friends = friendshipRepository.findByUser(currentUser)
                .stream().map(f -> f.getFriend()).collect(Collectors.toList());

        //Mapa que guarda o ID dos amigos e a quantidade de grupos em comum
        Map<Long, Integer> friendGroupCount = new HashMap<>();
        //Percorre todos os grupos
        for (Group g : existingGroups) {
            //analisa cada membro desse grupo
            for (User member : g.getMembers()) {
                //se o membro for o próprio usuário, ele irá ignorá-lo
                if (!member.getId().equals(currentUser.getId())) {
                    //soma +1 pra cada vez que o amigo aparecer junto dele em um grupo
                    friendGroupCount.merge(member.getId(), 1, Integer::sum);
                }
            }
        }

        //Mapa que guarda o ID do amigo e o total de despesas Pagas
        Map<Long, BigDecimal> friendExpenseTotal = new HashMap<>();
        //Percorre todos os grupos
        for (Group g : existingGroups) {
            //Busca despesas do Grupo
            List<Expense> expenses = expenseRepository.findByGroupId(g.getId());
            //Percorre as despesas
            for (Expense e : expenses) {
                //Pega o id de quem pagou
                Long payerId = e.getPayer().getId();
                //Ignora o usuário atual
                if (!payerId.equals(currentUser.getId())) {
                    //Soma o valor da despesa ao total já gasto pelo amigo
                    friendExpenseTotal.merge(payerId, e.getAmount(), BigDecimal::add);
                }
            }
        }

        //Monta o Prompt pra enviar pra IA
        String prompt = buildPrompt(currentUser, friends, existingGroups, friendGroupCount, friendExpenseTotal, context);

        try {
            //Faz chamada pra API da Groq
            String aiResponse = callGroqApi(prompt);
            //Converte o JSON em DTO
            return parseAiResponse(aiResponse);
        } catch (Exception e) {
            //Caso a IA falhe, leva as informações para o Fallback trazer a sugestão padrão
            return buildFallbackSuggestion(friends, friendGroupCount, context);
        }
    }

    //Método para construir o Prompt para ser enviado para a IA
    private String buildPrompt(User user, List<User> friends, List<Group> existingGroups,
                                Map<Long, Integer> friendGroupCount, Map<Long, BigDecimal> friendExpenseTotal,
                                String context) {
        //A escolha de StringBuilder se dá pq é melhor para concatenar strings grandes
        StringBuilder sb = new StringBuilder();

        //Definição do papel da IA
        sb.append("Você é um assistente especializado em divisão de despesas. ");
        sb.append("Analise os dados abaixo e sugira um novo grupo de divisão de despesas para o usuário.\n\n");

        //Envia os Dados do Usuário
        sb.append("=== USUÁRIO ===\n");
        sb.append("Nome: ").append(user.getName()).append("\n\n");

        //Analisa se existe contexto no Body da requisição e adiciona se ele existir
        if (context != null && !context.isBlank()) {
            sb.append("=== CONTEXTO DO USUÁRIO ===\n");
            sb.append(context).append("\n\n");
        }

        //Envia a Lista de Amigos Disponíveis
        sb.append("=== AMIGOS DISPONÍVEIS ===\n");
        //Para cada amigo em Amigos
        for (User friend : friends) {
            //Define a quantidade de grupos juntos
            int groupsTogether = friendGroupCount.getOrDefault(friend.getId(), 0);
            //Total de despesas desse amigo
            BigDecimal totalExpenses = friendExpenseTotal.getOrDefault(friend.getId(), BigDecimal.ZERO);
            //Envia as informações do ID, de NOME, do groupsTogether e de totalExpenses
            sb.append(String.format("- ID: %d | Nome: %s | Grupos juntos: %d | Total despesas: R$ %.2f\n",
                    friend.getId(), friend.getName(), groupsTogether, totalExpenses));
        }

        //Envia Grupos Existentes
        sb.append("\n=== GRUPOS EXISTENTES ===\n");
        //Para cada grupo g em grupos existentes
        for (Group g : existingGroups) {
            //Envia o nome do grupo e a quantidade de membros
            sb.append(String.format("- '%s' (%d membros)\n", g.getName(), g.getMembers().size()));
        }

        //Segue as Instruções do Prompt para que ele retorne um JSON válido com base nas informações anteriores
        sb.append("\n=== INSTRUÇÕES ===\n");
        sb.append("Com base nesses dados, sugira um novo grupo. Responda APENAS com JSON válido, sem markdown, no formato:\n");
        sb.append("{\n");
        sb.append("  \"suggestedName\": \"Nome do Grupo\",\n");
        sb.append("  \"suggestedDescription\": \"Descrição breve do grupo\",\n");
        sb.append("  \"suggestedMemberIds\": [lista de IDs dos amigos sugeridos],\n");
        sb.append("  \"reasoning\": \"Explicação curta do motivo desta sugestão\"\n");
        sb.append("}");

        //retornar o prompt completo
        return sb.toString();
    }

    //Método para chamar a API do Groq
    private String callGroqApi(String prompt) throws Exception {
        //Cria cliente HTTP
        HttpClient client = HttpClient.newHttpClient();

        //monta o corpo da requisição em JSON
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", "llama-3.1-8b-instant",
                "max_tokens", 1024,
                "temperature", 0.7,
                //conversa enviada para a IA 
                "messages", List.of(
                        //Mensagem do sistema
                        Map.of("role", "system", "content",
                                "Você é um assistente de divisão de despesas. Responda sempre em JSON puro, sem markdown."),
                        //Mensagem do Usuario
                        Map.of("role", "user", "content", prompt)
                )
        ));

        //Monta Requisição HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + groqApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Groq API error: " + response.statusCode() + " - " + response.body());
        }


        JsonNode root = objectMapper.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    private GroupSuggestionDTO parseAiResponse(String aiText) throws Exception {
        String cleanJson = aiText.trim()
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        JsonNode node = objectMapper.readTree(cleanJson);

        List<Long> memberIds = new ArrayList<>();
        JsonNode idsNode = node.path("suggestedMemberIds");
        if (idsNode.isArray()) {
            for (JsonNode idNode : idsNode) {
                memberIds.add(idNode.asLong());
            }
        }

        return new GroupSuggestionDTO(
                node.path("suggestedName").asText("Novo Grupo"),
                node.path("suggestedDescription").asText(""),
                memberIds,
                node.path("reasoning").asText("")
        );
    }

    private GroupSuggestionDTO buildFallbackSuggestion(List<User> friends,
                                                        Map<Long, Integer> friendGroupCount, String context) {
        List<Long> topFriends = friendGroupCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        String name = (context != null && !context.isBlank()) ? context : "Novo Grupo";
        return new GroupSuggestionDTO(
                name,
                "Grupo sugerido com base no seu histórico de divisões.",
                topFriends,
                "Sugestão baseada nos amigos com quem você mais divide despesas."
        );
    }
}
