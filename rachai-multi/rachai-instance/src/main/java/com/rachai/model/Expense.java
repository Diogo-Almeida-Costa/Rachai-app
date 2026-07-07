package com.rachai.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.framework.extension.resource.IResource;
import com.rachai.model.ExpenseSplit;

@Entity
@Table(name = "tb_expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


public class Expense implements IResource<BigDecimal> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseSplit> splits = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return Objects.equals(id, expense.id);
    }


    @Override
    public String getName() {
        // Mapeia o conceito "Name" do framework para a sua coluna "description"
        return this.description; 
    }

    @Override
    public BigDecimal getTotalValue() {
        // Mapeia o "TotalValue" genérico para a sua coluna "amount"
        return this.amount;
    }

    @Override
    public void setTotalValue(BigDecimal value) {
        // Permite que o Core (ou o OCR) defina o valor diretamente na sua coluna
        this.amount = value;
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    //Métodos Auxiliares para gerenciar os splits
    public void addSplit(ExpenseSplit split) {
        this.splits.add(split);
        split.setExpense((Expense) this);
    }

    public void removeSplit(ExpenseSplit split) {
        this.splits.remove(split);
        split.setExpense(null);
    }

    @Override
    public Long getId() {
        return this.id;
    }
}