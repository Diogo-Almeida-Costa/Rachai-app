package com.rachai.framework.extension.ai;

public interface IAIConfig {
    String getSystemInstructions();
    String getDomainContext();
    String getResponseSchema();
}
