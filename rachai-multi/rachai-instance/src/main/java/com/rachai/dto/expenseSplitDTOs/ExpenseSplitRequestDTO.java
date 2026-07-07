package com.rachai.dto.expenseSplitDTOs;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplitRequestDTO {
    private Long debtorId;
    private BigDecimal share;
}