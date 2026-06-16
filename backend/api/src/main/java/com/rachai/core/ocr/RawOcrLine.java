package com.rachai.core.ocr;

/**
 * Representação genérica e imutável de uma linha extraída de qualquer imagem.
 */
public record RawOcrLine(String textContent, Double numericValue, Object metadata) {
}