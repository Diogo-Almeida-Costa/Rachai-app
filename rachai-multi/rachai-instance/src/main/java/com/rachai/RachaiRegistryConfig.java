package com.rachai;

import com.framework.core.Framework;
import com.framework.extension.ai.IAIConfig;
import com.rachai.ai.RachAIConfig;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;


@Configuration
public class RachaiRegistryConfig {

    private final Framework framework;
    private final RachAIConfig rachAIConfig;

    public RachaiRegistryConfig(Framework framework, RachAIConfig rachAIConfig) {
        this.framework = framework;
        this.rachAIConfig = rachAIConfig;
    }

    @PostConstruct
    public void registerRachaiServices() {
        // Registra a extensão flexível de IA no localizador do Framework
        framework.getRegistry().register(IAIConfig.class, rachAIConfig);
    }
    
}
