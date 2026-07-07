package com.rachai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.rachai.dto.GroupSuggestionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.core.client.GroqApiClient;
import com.rachai.model.Expense;
import com.rachai.model.Group;
import com.rachai.model.User;
import com.rachai.repository.ExpenseRepository;
import com.rachai.repository.FriendshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AiGroupSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(AiGroupSuggestionService.class);

    @Autowired
    private GroqApiClient groqApiClient;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GroupSuggestionDTO suggestGroup(User currentUser, List<Group> existingGroups, String context) {
        logger.info("Attempting to generate intelligent group suggestions for user ID: {}", currentUser.getId());

        List<User> friends = friendshipRepository.findByUser(currentUser).stream().map(f -> f.getFriend()).toList();

        Map<Long, Integer> friendGroupCount = new HashMap<>();
        Map<Long, BigDecimal> friendExpenseTotal = new HashMap<>();

        for (Group g : existingGroups) {
            for (User member : g.getMembers()) {
                if (!member.getId().equals(currentUser.getId())) {
                    friendGroupCount.merge(member.getId(), 1, Integer::sum);
                }
            }
        }

        if (!existingGroups.isEmpty()) {
            List<Long> groupIds = existingGroups.stream().map(Group::getId).toList();
            List<Expense> allExpenses = expenseRepository.findByGroupIdIn(groupIds);

            for (Expense e : allExpenses) {
                Long payerId = e.getPayer().getId();
                if (!payerId.equals(currentUser.getId())) {
                    friendExpenseTotal.merge(payerId, e.getAmount(), BigDecimal::add);
                }
            }
        }

        String prompt = buildPrompt(currentUser, friends, existingGroups, friendGroupCount, friendExpenseTotal, context);
        String systemMessage = "Você é um assistente de divisão de despesas. Responda sempre em JSON puro, sem marcações markdown de bloco de código.";

        try {
            String aiResponse = groqApiClient.chat(systemMessage, prompt);
            return parseAiResponse(aiResponse);
        } catch (Exception e) {
            logger.warn("GROQ API failed or returned bad JSON. Activating local fallback engine. Reason: {}", e.getMessage());
            return buildFallbackSuggestion(friends, friendGroupCount, context);
        }
    }

    private String buildPrompt(User user, List<User> friends, List<Group> existingGroups, Map<Long, Integer> friendGroupCount, Map<Long, BigDecimal> friendExpenseTotal, String context) {
        StringBuilder sb = new StringBuilder();

        sb.append("Você é um assistente especializado em divisão de despesas. ");
        sb.append("Analise os dados abaixo e sugira um novo grupo de divisão de despesas para o usuário.\n\n");

        sb.append("=== USUÁRIO ===\n");
        sb.append("Nome: ").append(user.getName()).append("\n\n"); // Ajustado para getName() igual ao seu Model

        if (context != null && !context.isBlank()) {
            sb.append("=== CONTEXTO DO USUÁRIO ===\n");
            sb.append(context).append("\n\n");
        }

        sb.append("=== AMIGOS DISPONÍVEIS ===\n");
        for (User friend : friends) {
            int groupsTogether = friendGroupCount.getOrDefault(friend.getId(), 0);
            BigDecimal totalExpenses = friendExpenseTotal.getOrDefault(friend.getId(), BigDecimal.ZERO);
            sb.append(String.format("- ID: %d | Nome: %s | Grupos juntos: %d | Total despesas: R$ %.2f\n", 
                    friend.getId(), friend.getName(), groupsTogether, totalExpenses));
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

    private GroupSuggestionDTO parseAiResponse(String aiText) throws JsonProcessingException {
        String cleanJson = aiText.trim()
                .replaceAll("(?s)^.*?\\{", "{")
                .replaceAll("(?s)\\}.*?$", "}");

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

    private GroupSuggestionDTO buildFallbackSuggestion(List<User> friends, Map<Long, Integer> friendGroupCount, String context) {
        List<Long> topFriends = friendGroupCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        String name = (context != null && !context.isBlank()) ? context : "Grupo de Resenha";
        return new GroupSuggestionDTO(
                name,
                "Grupo sugerido com base nas suas interações frequentes.",
                topFriends,
                "A IA está indisponível no momento, mas sugerimos este grupo baseado nos amigos com quem você mais compartilha grupos atuais."
        );
    }
}