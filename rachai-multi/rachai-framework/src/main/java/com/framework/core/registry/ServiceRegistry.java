package com.framework.core.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;


@Component

public class ServiceRegistry {
     private final Map<Class<?>, Object> registry = new ConcurrentHashMap<>();
 
    public <T> void register(Class<T> serviceInterface, T implementation) {
        registry.put(serviceInterface, implementation);
    }
 
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> serviceInterface) {
        T service = (T) registry.get(serviceInterface);
        if (service == null) {
            throw new IllegalStateException("Serviço não registrado para a interface: " + serviceInterface.getName());
        }
        return service;
    }
 
    public boolean has(Class<?> serviceInterface) {
        return registry.containsKey(serviceInterface);
    }
    
}
