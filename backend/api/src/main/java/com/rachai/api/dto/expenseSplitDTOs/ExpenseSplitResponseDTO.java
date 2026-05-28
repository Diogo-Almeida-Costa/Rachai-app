package com.rachai.api.dto.expenseSplitDTOs;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplitResponseDTO {
    private Long id;
    private Long debtorId;
    private String debtorName;
    private BigDecimal share;
}
