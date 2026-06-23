package com.rachai.api.instance.resource;

import com.rachai.framework.extension.resource.IResource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResource implements IResource {
    private Long id;
    private String name;
    private BigDecimal amount;
}
