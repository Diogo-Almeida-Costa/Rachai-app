package com.frequencia.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frequencia.dto.AnaliseRiscoDTO;
import com.frequencia.dto.EstatisticaAlunoDTO;
import com.framework.core.Framework;
import com.framework.core.client.GroqApiClient;
import com.framework.core.context.ContextManager;
import com.framework.extension.ai.IAIConfig;
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
 * configurada ou a chamada falhar por qualquer motivo (rede, cota, etc.), cai
 * em um fallback local que calcula o mesmo tipo de resultado diretamente das
 * estatísticas já calculadas no servidor, para a funcionalidade nunca ficar
 * completamente quebrada.
 */
@Service
public class FrequenciaAIService {

    private static final Logger logger = LoggerFactory.getLogger(FrequenciaAIService.class);

    @Autowired
    private Framework framework;

    @Autowired
    private ContextManager contextManager;

    @Autowired
    private GroqApiClient groqApiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Mantido para fins de transparência/depuração: mostra o prompt puro que é enviado à IA. */
    public String gerarPromptDeRisco(List<EstatisticaAlunoDTO> estatisticas) {
        IAIConfig aiConfig = framework.getRegistry().get(IAIConfig.class);
        return contextManager.buildPrompt(aiConfig, estatisticas);
    }

    /** Chama de fato a IA (Groq) e retorna a análise de risco já interpretada. */
    public AnaliseRiscoDTO analisarRisco(List<EstatisticaAlunoDTO> estatisticas) {
        String prompt = gerarPromptDeRisco(estatisticas);

        try {
            String systemMessage = "Você é um assistente que responde estritamente em JSON puro, "
                    + "sem markdown e sem texto fora do JSON.";
            String respostaBruta = groqApiClient.chat(systemMessage, prompt);
            return interpretarResposta(respostaBruta);
        } catch (Exception e) {
            logger.warn("Falha ao chamar a IA (Groq). Usando fallback local. Motivo: {}", e.getMessage());
            return calcularFallbackLocal(estatisticas);
        }
    }

    private AnaliseRiscoDTO interpretarResposta(String respostaBruta) throws Exception {
        String jsonLimpo = respostaBruta.trim()
                .replaceAll("(?s)^.*?\\{", "{")
                .replaceAll("(?s)\\}[^}]*$", "}");

        JsonNode root = objectMapper.readTree(jsonLimpo);
        JsonNode alunosNode = root.path("alunosEmRisco");

        List<AnaliseRiscoDTO.AlunoRiscoDTO> alunos = new ArrayList<>();
        if (alunosNode.isArray()) {
            for (JsonNode alunoNode : alunosNode) {
                alunos.add(new AnaliseRiscoDTO.AlunoRiscoDTO(
                        alunoNode.path("alunoId").asLong(),
                        alunoNode.path("nome").asText(""),
                        alunoNode.path("percentualFaltas").asDouble(0.0)
                ));
            }
        }

        String observacao = root.path("observacaoGeral").asText(
                root.path("recomendacaoPreditiva").asText(""));

        return new AnaliseRiscoDTO(alunos, observacao, "ia");
    }

    private AnaliseRiscoDTO calcularFallbackLocal(List<EstatisticaAlunoDTO> estatisticas) {
        List<AnaliseRiscoDTO.AlunoRiscoDTO> emRisco = estatisticas.stream()
                .filter(EstatisticaAlunoDTO::emRisco)
                .map(e -> new AnaliseRiscoDTO.AlunoRiscoDTO(e.alunoId(), e.alunoNome(), e.percentualFaltas()))
                .toList();

        String observacao = emRisco.isEmpty()
                ? "Nenhum aluno ultrapassou o limite de faltas no momento."
                : emRisco.size() + " aluno(s) ultrapassaram o limite de faltas e precisam de atenção.";

        return new AnaliseRiscoDTO(emRisco, observacao, "fallback-local");
    }
}
