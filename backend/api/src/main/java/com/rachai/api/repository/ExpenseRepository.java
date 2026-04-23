package com.rachai.api.repository;

import com.rachai.api.model.Expense;
import com.rachai.api.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroupId(Long groupId);
}
