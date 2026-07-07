package com.taskflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.core.Framework;
import com.framework.core.client.GroqApiClient;
import com.framework.core.context.ContextManager;
import com.framework.extension.ai.IAIConfig;
import com.taskflow.dto.SugestaoTarefasDTO;
import com.taskflow.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Diferente da versão anterior (que só montava o prompt e nunca o enviava a
 * lugar nenhum), este service agora efetivamente chama a IA (Groq) através
 * do GroqApiClient — peça fixa do framework, a mesma usada em outras
 * instâncias — e interpreta a resposta. Se a chave da Groq não estiver
 * configurada ou a chamada falhar por qualquer motivo, cai em um fallback
 * local simples (sem inventar tarefas novas, já que isso exige de fato uma
 * IA generativa) para a funcionalidade nunca ficar completamente quebrada.
 */
@Service
public class TaskFlowAIService {

    private static final Logger logger = LoggerFactory.getLogger(TaskFlowAIService.class);

    @Autowired
    private Framework framework;

    @Autowired
    private ContextManager contextManager;

    @Autowired
    private GroqApiClient groqApiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Mantido para fins de transparência/depuração: mostra o prompt puro que é enviado à IA. */
    public String gerarPromptDeSugestao(List<Task> tarefasPendentes) {
        IAIConfig aiConfig = framework.getRegistry().get(IAIConfig.class);
        return contextManager.buildPrompt(aiConfig, tarefasPendentes);
    }

    /** Chama de fato a IA (Groq) e retorna a sugestão de tarefas já interpretada. */
    public SugestaoTarefasDTO gerarSugestao(List<Task> tarefasPendentes) {
        String prompt = gerarPromptDeSugestao(tarefasPendentes);

        try {
            String systemMessage = "Você é um assistente que responde estritamente em JSON puro, "
                    + "sem markdown e sem texto fora do JSON.";
            String respostaBruta = groqApiClient.chat(systemMessage, prompt);
            return interpretarResposta(respostaBruta);
        } catch (Exception e) {
            logger.warn("Falha ao chamar a IA (Groq). Usando fallback local. Motivo: {}", e.getMessage());
            return calcularFallbackLocal(tarefasPendentes);
        }
    }

    private SugestaoTarefasDTO interpretarResposta(String respostaBruta) throws Exception {
        String jsonLimpo = respostaBruta.trim()
                .replaceAll("(?s)^.*?\\{", "{")
                .replaceAll("(?s)\\}[^}]*$", "}");

        JsonNode root = objectMapper.readTree(jsonLimpo);
        JsonNode tarefasNode = root.path("tarefasSugeridas");

        List<SugestaoTarefasDTO.TarefaSugeridaDTO> tarefas = new ArrayList<>();
        if (tarefasNode.isArray()) {
            for (JsonNode tarefaNode : tarefasNode) {
                tarefas.add(new SugestaoTarefasDTO.TarefaSugeridaDTO(
                        tarefaNode.path("titulo").asText(""),
                        tarefaNode.path("prioridade").asText("MEDIA"),
                        tarefaNode.path("motivo").asText("")
                ));
            }
        }

        String recomendacao = root.path("recomendacaoPreditiva").asText("");
        return new SugestaoTarefasDTO(tarefas, recomendacao, "ia");
    }

    private SugestaoTarefasDTO calcularFallbackLocal(List<Task> tarefasPendentes) {
        long altas = tarefasPendentes.stream().filter(t -> t.getPriority() == Task.Priority.ALTA).count();

        String recomendacao;
        if (tarefasPendentes.isEmpty()) {
            recomendacao = "Nenhuma tarefa pendente no momento.";
        } else if (altas > 0) {
            recomendacao = altas + " tarefa(s) de prioridade ALTA pendente(s). Priorize a delegação delas "
                    + "antes de adicionar novas tarefas ao grupo, para evitar sobrecarga.";
        } else {
            recomendacao = "A carga de tarefas pendentes parece administrável, sem prioridades ALTA em aberto.";
        }

        // Sem uma IA generativa disponível, não é honesto "inventar" tarefas correlatas;
        // o fallback local se limita à recomendação preditiva, que pode ser calculada
        // de forma determinística a partir dos dados já existentes.
        return new SugestaoTarefasDTO(List.of(), recomendacao, "fallback-local");
    }
}
