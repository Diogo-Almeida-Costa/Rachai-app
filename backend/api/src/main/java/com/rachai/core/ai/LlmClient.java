package com.rachai.core.ai;

/**
 * Ponto Flexível: Contrato para os adaptadores das LLMs utilizadas (Groq,
 * Gemini, Claude).
 */
public interface LlmClient {
    String invokeModel(String instructions, String payload);
}