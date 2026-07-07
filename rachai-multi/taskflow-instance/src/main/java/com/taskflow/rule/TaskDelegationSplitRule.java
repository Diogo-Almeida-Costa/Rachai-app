package com.taskflow.rule;

import com.framework.core.exception.BusinessException;
import com.framework.extension.rule.ISplitRule;
import com.framework.extension.user.IUser;
import com.taskflow.model.Collaborator;
import com.taskflow.model.Project;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regra de divisão específica desta instância (equivalente à SplitRule de
 * rachai-instance). Em vez de dividir igualmente ou por consumo exato, aqui a
 * carga total de tarefas pendentes (já ponderada por prioridade, calculada em
 * Project.getTotalValue()) é delegada de forma proporcional à disponibilidade
 * de cada colaborador: quem está menos ocupado recebe uma fatia maior da
 * carga nova; quem já está mais ocupado recebe menos.
 */
public class TaskDelegationSplitRule implements ISplitRule<Project> {

    @Override
    public Map<IUser, BigDecimal> split(Project resource, List<IUser> participants) {
        if (participants == null || participants.isEmpty()) {
            throw new BusinessException("Não é possível delegar tarefas sem participantes no grupo");
        }

        BigDecimal cargaTotal = resource.getTotalValue();
        if (cargaTotal == null || cargaTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Não há carga de tarefas pendente para delegar");
        }

        // Disponibilidade de cada colaborador = inverso da ocupação atual (0-100).
        // Mantém um piso de 1 para que ninguém fique fora da delegação.
        Map<IUser, BigDecimal> disponibilidadePorColaborador = new LinkedHashMap<>();
        BigDecimal somaDisponibilidade = BigDecimal.ZERO;

        for (IUser participant : participants) {
            int ocupacao = (participant instanceof Collaborator collaborator)
                    ? Math.max(0, Math.min(100, collaborator.getOccupationLevel()))
                    : 50; // fallback neutro caso o IUser não seja um Collaborator

            BigDecimal disponibilidade = BigDecimal.valueOf(Math.max(100 - ocupacao, 1));
            disponibilidadePorColaborador.put(participant, disponibilidade);
            somaDisponibilidade = somaDisponibilidade.add(disponibilidade);
        }

        Map<IUser, BigDecimal> delegacao = new LinkedHashMap<>();
        BigDecimal jaDistribuido = BigDecimal.ZERO;

        for (int i = 0; i < participants.size(); i++) {
            IUser participant = participants.get(i);
            BigDecimal disponibilidade = disponibilidadePorColaborador.get(participant);

            BigDecimal parte;
            if (i == participants.size() - 1) {
                // Último participante fica com o resto exato, evitando erro de arredondamento
                parte = cargaTotal.subtract(jaDistribuido).setScale(2, RoundingMode.HALF_UP);
            } else {
                BigDecimal proporcao = disponibilidade.divide(somaDisponibilidade, 6, RoundingMode.HALF_UP);
                parte = cargaTotal.multiply(proporcao).setScale(2, RoundingMode.HALF_UP);
                jaDistribuido = jaDistribuido.add(parte);
            }

            delegacao.put(participant, parte);
        }

        return delegacao;
    }
}
