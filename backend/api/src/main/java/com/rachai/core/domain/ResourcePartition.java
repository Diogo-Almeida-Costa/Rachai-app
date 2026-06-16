package com.rachai.core.domain;

import com.rachai.api.model.User;

/**
 * Ponto Flexível: Representa o pedaço ou a participação de um usuário específico no recurso.
 * Substitui o acoplamento direto com o 'share' numérico de despesas.
 */
public interface ResourcePartition {
    User getUser();
    void setPartitionValue(Object value); // Permite Double, Integer (horas) ou Strings (tarefas)
}