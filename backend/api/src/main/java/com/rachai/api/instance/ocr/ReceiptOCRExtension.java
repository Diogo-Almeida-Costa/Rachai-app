package com.rachai.api.instance.ocr;

import com.rachai.framework.extension.ocr.IOCRExtension;
import com.rachai.api.instance.resource.ExpenseResource;
import java.math.BigDecimal;

public class ReceiptOCRExtension implements IOCRExtension<ExpenseResource> {
    @Override
    public ExpenseResource parseAndValidate(String rawOcrData) {
        // Lógica para converter o retorno do Tabscanner em um ExpenseResource
        // Aqui simulamos a extração de dados
        ExpenseResource resource = new ExpenseResource();
        resource.setName("Despesa de Cupom Fiscal");
        resource.setAmount(new BigDecimal("100.00")); // Valor mockado para exemplo
        return resource;
    }

    @Override
    public String getDocumentSchema() {
        return "Cupom Fiscal: [Data, Estabelecimento, Itens, Total]";
    }
}
