package com.rachai.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rachai.api.client.GroqApiClient;
import com.rachai.api.dto.GroupSuggestionDTO;
import com.rachai.api.exception.BusinessException;
import com.rachai.api.model.Expense;
import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.repository.ExpenseRepository;
import com.rachai.api.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiGroupSuggestionService {

    @Autowired
    private GroqApiClient groqApiClient;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GroupSuggestionDTO suggestGroup(User currentUser, List<Group> existingGroups, String context) {
        List<User> friends = friendshipRepository.findByUser(currentUser).stream().map(f -> f.getFriend()).collect(Collectors.toList());

        Map<Long, Integer> friendGroupCount = new HashMap<>();

        for (Group g : existingGroups) {
            for (User member : g.getMembers()) {
                if (!member.getId().equals(currentUser.getId())) {
                    friendGroupCount.merge(member.getId(), 1, Integer::sum);
                }
            }
        }

        Map<Long, BigDecimal> friendExpenseTotal = new HashMap<>();
        for (Group g : existingGroups) {
            List<Expense> expenses = expenseRepository.findByGroupId(g.getId());
            for (Expense e : expenses) {
                Long payerId = e.getPayer().getId();
                if (!payerId.equals(currentUser.getId())) {
                    friendExpenseTotal.merge(payerId, e.getAmount(), BigDecimal::add);
                }
            }
        }

        String prompt = buildPrompt(currentUser, friends, existingGroups, friendGroupCount, friendExpenseTotal, context);

        String systemMessage = "Você é um assistente de divisão de despesas. Responda sempre em JSON puro, sem markdown.";
        String aiResponse = groqApiClient.chat(systemMessage, prompt);

        return parseAiResponse(aiResponse);
    }

    private String buildPrompt(User user, List<User> friends, List<Group> existingGroups, Map<Long, Integer> friendGroupCount, Map<Long, BigDecimal> friendExpenseTotal, String context) {
        StringBuilder sb = new StringBuilder();

        sb.append("Você é um assistente especializado em divisão de despesas. ");
        sb.append("Analise os dados abaixo e sugira um novo grupo de divisão de despesas para o usuário.\n\n");

        sb.append("=== USUÁRIO ===\n");
        sb.append("Nome: ").append(user.getFirstName()).append("\n\n");

        if (context != null && !context.isBlank()) {
            sb.append("=== CONTEXTO DO USUÁRIO ===\n");
            sb.append(context).append("\n\n");
        }

        sb.append("=== AMIGOS DISPONÍVEIS ===\n");
        for (User friend : friends) {
            int groupsTogether = friendGroupCount.getOrDefault(friend.getId(), 0);
            BigDecimal totalExpenses = friendExpenseTotal.getOrDefault(friend.getId(), BigDecimal.ZERO);
            sb.append(String.format("- ID: %d | Nome: %s | Grupos juntos: %d | Total despesas: R$ %.2f\n", friend.getId(), friend.getFirstName(), groupsTogether, totalExpenses));
        }

        sb.append("\n=== GRUPOS EXISTENTES ===\n");
        for (Group g : existingGroups) {
            sb.append(String.format("- '%s' (%d membros)\n", g.getName(), g.getMembers().size()));
        }

        sb.append("\n=== INSTRUÇÕES ===\n");
        sb.append("Com base nesses dados, sugira um novo grupo. Responda APENAS com JSON válido, sem markdown, no formato:\n");
        sb.append("{\n");
        sb.append("  \"suggestedName\": \"Nome do Grupo\",\n");
        sb.append("  \"suggestedDescription\": \"Descrição breve do grupo\",\n");
        sb.append("  \"suggestedMemberIds\": [lista de IDs dos amigos sugeridos],\n");
        sb.append("  \"reasoning\": \"Explicação curta do motivo desta sugestão\"\n");
        sb.append("}");

        return sb.toString();
    }

    private GroupSuggestionDTO parseAiResponse(String aiText) {
        try {
            String cleanJson = aiText.trim().replaceAll("```json", "").replaceAll("```", "").trim();

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
                    node.path("reasoning").asText(""));

        } catch (JsonProcessingException e) {
            throw new BusinessException("Erro ao processar sugestão da IA: " + e.getMessage());
        }
    }

    private GroupSuggestionDTO buildFallbackSuggestion(List<User> friends, Map<Long, Integer> friendGroupCount, String context) {
        List<Long> topFriends = friendGroupCount.entrySet().stream().sorted(Map.Entry.<Long, Integer>comparingByValue().reversed()).limit(3).map(Map.Entry::getKey).collect(Collectors.toList());

        String name = (context != null && !context.isBlank()) ? context : "Novo Grupo";
        return new GroupSuggestionDTO(name,"Grupo sugerido com base no seu histórico de divisões.", topFriends ,"Sugestão baseada nos amigos com quem você mais divide despesas.");
    }
}