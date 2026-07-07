package com.framework.extension.resource;

import java.math.BigDecimal;

public interface IResource<T> {
    Long getId();
    String getName();
    T getTotalValue();

    void setTotalValue(T value);

}
