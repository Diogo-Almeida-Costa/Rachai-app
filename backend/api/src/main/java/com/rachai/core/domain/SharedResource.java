package com.rachai.core.domain;

/**
 * Ponto Flexível: Representa a entidade/dado bruto que será rateado.
 * Pode ser uma Despesa (Financeiro), uma Aula (Frequência) ou um Card (Tarefas)[cite: 13].
 */
public interface SharedResource {
    Long getId();
    String getDescription();
}
