package com.rachai.core.service;

import com.rachai.core.domain.SharedResource;
import com.rachai.core.domain.ResourcePartition;
import com.rachai.api.exception.BusinessException;
import java.util.List;

/**
 * Core do algoritmo de partição. Garante o ciclo de dados compartilhados[cite:
 * 7].
 */
public abstract class AbstractSplitEngine<R extends SharedResource, P extends ResourcePartition> {

    /**
     * Ponto Fixo: O fluxo macro de validação, execução e consolidação do ciclo é
     * rígido[cite: 7].
     */
    public final void processResourceDistribution(R resource, List<P> partitions) {
        if (partitions == null || partitions.isEmpty()) {
            throw new BusinessException("A lista de partições não pode estar vazia.");
        }

        // 1. Ponto Flexível: Valida as regras matemáticas específicas da instância
        validatePartitionRules(resource, partitions);

        // 2. Ponto Flexível: Executa a lógica de rateio do negócio
        applyDistributionLogic(resource, partitions);

        // 3. Ponto Fixo: Consolida e persiste o estado final compartilhado[cite: 7]
        persistDistributionState(resource);
    }

    protected abstract void validatePartitionRules(R resource, List<P> partitions);

    protected abstract void applyDistributionLogic(R resource, List<P> partitions);

    protected abstract void persistDistributionState(R resource);
}