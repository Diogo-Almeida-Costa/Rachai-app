package com.framework.core.context;

import java.util.HashMap;
import java.util.Map;

public class FrameworkContext {
    private static final ThreadLocal<FrameworkContext> current = ThreadLocal.withInitial(FrameworkContext::new);

    private final Map<String, Object> attributes = new HashMap<>();

    public static FrameworkContext getCurrent() {
        return current.get();
    }

    public static void clear() {
        current.remove();
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        return (T) attributes.get(key);
    }
}
