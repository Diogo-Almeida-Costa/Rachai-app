package com.framework.core;

import com.framework.core.context.FrameworkContext;
import com.framework.core.registry.ServiceRegistry;

import org.springframework.stereotype.Component;

@Component

public class Framework {
     private final ServiceRegistry registry;
 
    public Framework(ServiceRegistry registry) {
        this.registry = registry;
    }
 
    public ServiceRegistry getRegistry() {
        return this.registry;
    }
 
    public FrameworkContext initializeContext() {
        FrameworkContext.clear();
        return FrameworkContext.getCurrent();
    }
 
    public void terminateContext() {
        FrameworkContext.clear();
    }
}
