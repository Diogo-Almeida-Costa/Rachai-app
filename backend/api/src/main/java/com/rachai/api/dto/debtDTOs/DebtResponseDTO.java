package com.rachai.api.dto.debtDTOs;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DebtResponseDTO {
    private Long id;
    private Long debtorId;
    private String debtorName;
    private Long creditorId;
    private String creditorName;
    private Long groupId;
    private BigDecimal amount;
    private boolean settled;
    private LocalDateTime createdAt;
}