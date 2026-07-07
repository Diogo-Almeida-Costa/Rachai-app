package com.taskflow.config;

import com.framework.core.Framework;
import com.framework.extension.ai.IAIConfig;
import com.taskflow.ai.TaskAIConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Ponto de "conexão" desta instância com o framework (equivalente a
 * FrequenciaRegistryConfig / RachaiRegistryConfig): registra a implementação
 * concreta TaskAIConfig contra a interface genérica IAIConfig dentro do
 * ServiceRegistry, para que o ContextManager (peça fixa do core) consiga
 * localizá-la sem conhecer esta instância.
 */
@Component
public class TaskFlowRegistryConfig {

    @Autowired
    private Framework framework;

    @Autowired
    private TaskAIConfig taskAIConfig;

    @PostConstruct
    public void registrarExtensoes() {
        framework.getRegistry().register(IAIConfig.class, taskAIConfig);
    }
}
