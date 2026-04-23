package com.rachai.api.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "debts")
@NoArgsConstructor
@AllArgsConstructor
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor; // Quem deve

    @ManyToOne
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor; // Quem recebe

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private boolean settled = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
