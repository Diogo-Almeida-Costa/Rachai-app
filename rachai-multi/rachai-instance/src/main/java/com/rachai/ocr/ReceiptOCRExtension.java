package com.rachai.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.core.exception.BusinessException;
import com.framework.extension.ocr.IOCRExtension;
import com.rachai.model.Expense;
 
import java.math.BigDecimal;

public class ReceiptOCRExtension implements IOCRExtension<Expense> {
      private final ObjectMapper objectMapper = new ObjectMapper();
 
    @Override
    public Expense parseAndValidate(String rawOcrData) {
        JsonNode root;
        try {
            root = objectMapper.readTree(rawOcrData);
        } catch (Exception e) {
            throw new BusinessException("Não foi possível interpretar o resultado do OCR do cupom fiscal");
        }
 
        JsonNode result = root.path("result");
 
        if (result.isMissingNode() || result.isNull()) {
            throw new BusinessException("O cupom fiscal não retornou um resultado válido de OCR");
        }
 
        JsonNode totalNode = result.path("total");
        if (totalNode.isMissingNode() || totalNode.isNull()) {
            throw new BusinessException("Não foi possível identificar o valor total do cupom fiscal");
        }
 
        BigDecimal total = totalNode.decimalValue();
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor total identificado no cupom fiscal é inválido");
        }
 
        String description = buildDescription(result);
 
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(total);
        return expense;
    }
 
    private String buildDescription(JsonNode result) {
        JsonNode lineItems = result.path("lineItems");
        int itemCount = lineItems.isArray() ? lineItems.size() : 0;
        return itemCount > 0
                ? "Despesa via cupom fiscal (" + itemCount + " itens)"
                : "Despesa via cupom fiscal";
    }
 
    @Override
    public String getDocumentSchema() {
        return "Cupom fiscal (recibo de compra) processado via Tabscanner: espera-se um "
                + "JSON com 'result.total' (valor total da compra) e 'result.lineItems' "
                + "(itens individuais do cupom).";
    }
}
