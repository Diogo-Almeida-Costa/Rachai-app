package com.frequencia.config;

import com.frequencia.ai.FrequenciaAIConfig;
import com.framework.core.Framework;
import com.framework.extension.ai.IAIConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aqui acontece a "conexão" de verdade com o framework: a instância
 * registra sua implementação concreta (FrequenciaAIConfig) contra a
 * interface genérica (IAIConfig) dentro do ServiceRegistry do Framework.
 */
@Component
public class FrequenciaRegistryConfig {

    @Autowired
    private Framework framework;

    @Autowired
    private FrequenciaAIConfig frequenciaAIConfig;

    @PostConstruct
    public void registrarExtensoes() {
        framework.getRegistry().register(IAIConfig.class, frequenciaAIConfig);
    }
}
