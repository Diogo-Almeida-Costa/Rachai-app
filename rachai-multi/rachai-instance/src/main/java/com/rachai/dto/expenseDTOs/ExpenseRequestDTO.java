package com.rachai.dto.expenseDTOs;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

import com.rachai.dto.expenseSplitDTOs.ExpenseSplitRequestDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequestDTO {
    private String description;
    private BigDecimal amount;
    private Long payerId;
    private Long groupId;
    private List<ExpenseSplitRequestDTO> splits;
}