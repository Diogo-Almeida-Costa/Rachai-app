package com.taskflow.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.core.exception.BusinessException;
import com.framework.extension.ocr.IOCRExtension;
import com.taskflow.model.Project;
import com.taskflow.model.Task;

import java.math.BigDecimal;

/**
 * Extensão de OCR desta instância (equivalente à ReceiptOCRExtension de
 * rachai-instance). Reaproveita o mesmo módulo fixo (OCRModule) e o mesmo
 * cliente de infraestrutura (TabscannerApiClient) do core: a única coisa que
 * muda é a interpretação do conteúdo capturado, que aqui é uma lista de
 * tarefas (post-it, quadro físico ou lista manuscrita) em vez de um cupom
 * fiscal.
 *
 * Convenção de mapeamento dos campos do Tabscanner para o domínio de tarefas:
 *  - "desc"      -> título da tarefa
 *  - "lineTotal" -> esforço estimado (horas/pontos)
 *  - "qty"       -> nível de prioridade (1 = baixa, 2 = média, 3 = alta)
 */
public class TaskListOCRExtension implements IOCRExtension<Project> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Project parseAndValidate(String rawOcrData) {
        JsonNode root;
        try {
            root = objectMapper.readTree(rawOcrData);
        } catch (Exception e) {
            throw new BusinessException("Não foi possível interpretar o resultado do OCR da lista de tarefas");
        }

        JsonNode result = root.path("result");
        if (result.isMissingNode() || result.isNull()) {
            throw new BusinessException("A lista de tarefas não retornou um resultado válido de OCR");
        }

        JsonNode lineItems = result.path("lineItems");
        if (!lineItems.isArray() || lineItems.isEmpty()) {
            throw new BusinessException("Nenhuma tarefa foi identificada na lista enviada");
        }

        Project project = new Project();
        project.setName("Projeto capturado via OCR");

        long taskId = 1L;
        for (JsonNode item : lineItems) {
            String titulo = item.path("desc").isMissingNode()
                    ? "Tarefa sem título"
                    : item.path("desc").asText("Tarefa sem título");

            BigDecimal esforco = (item.path("lineTotal").isMissingNode() || item.path("lineTotal").isNull())
                    ? BigDecimal.ONE
                    : item.path("lineTotal").decimalValue();
            if (esforco.compareTo(BigDecimal.ZERO) <= 0) {
                esforco = BigDecimal.ONE;
            }

            Task.Priority prioridade = inferirPrioridade(item.path("qty"));

            Task task = new Task(taskId++, null, titulo, prioridade, esforco);
            project.addTask(task);
        }

        return project;
    }

    private Task.Priority inferirPrioridade(JsonNode qtyNode) {
        if (qtyNode == null || qtyNode.isMissingNode() || qtyNode.isNull()) {
            return Task.Priority.MEDIA;
        }
        try {
            int qty = Integer.parseInt(qtyNode.asText());
            if (qty >= 3) return Task.Priority.ALTA;
            if (qty == 2) return Task.Priority.MEDIA;
            return Task.Priority.BAIXA;
        } catch (NumberFormatException e) {
            return Task.Priority.MEDIA;
        }
    }

    @Override
    public String getDocumentSchema() {
        return "Lista de tarefas (post-it, quadro físico ou lista manuscrita) processada via Tabscanner: espera-se "
                + "um JSON com 'result.lineItems', onde cada item possui 'desc' (título da tarefa), "
                + "'lineTotal' (esforço estimado em horas/pontos) e 'qty' (nível de prioridade: 1=baixa, 2=média, 3=alta).";
    }
}
