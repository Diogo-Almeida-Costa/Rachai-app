package com.rachai.dto.expenseDTOs;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.rachai.dto.expenseSplitDTOs.ExpenseSplitResponseDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponseDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private Long payerId;
    private String payerName;
    private Long groupId;
    private List<ExpenseSplitResponseDTO> splits;
    private LocalDateTime createdAt;
}
