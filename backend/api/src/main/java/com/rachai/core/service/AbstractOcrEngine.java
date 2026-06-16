package com.rachai.core.service;

import com.rachai.core.ocr.RawOcrLine;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Ponto Fixo: Serve como a porta de entrada de dados brutos por imagem do
 * framework.
 */
public abstract class AbstractOcrEngine<T> {

    /**
     * Fluxo rígido de processamento físico do arquivo.
     */
    public final T processDocument(MultipartFile file) {
        // Ponto Fixo: Faz o upload físico e extrai o texto bruto (Ex: via Tabscanner)
        List<RawOcrLine> rawLines = executeImageExtraction(file);

        // Ponto Flexível: Transforma o texto bruto no contrato esperado da instância
        // (Schema/Campos)
        return mapToInstanceSpecification(rawLines);
    }

    protected abstract List<RawOcrLine> executeImageExtraction(MultipartFile file);

    /**
     * Ponto Flexível: Cada instância sabe se lê comandas, listas de presença ou
     * post-its.
     */
    protected abstract T mapToInstanceSpecification(List<RawOcrLine> rawLines);
}