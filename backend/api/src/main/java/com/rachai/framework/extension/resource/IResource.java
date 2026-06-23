package com.rachai.framework.extension.resource;

import java.math.BigDecimal;

public interface IResource {
    Long getId();
    String getName();
    BigDecimal getAmount();
}
