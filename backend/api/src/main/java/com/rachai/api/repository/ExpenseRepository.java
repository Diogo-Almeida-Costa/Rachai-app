package com.rachai.api.repository;

import com.rachai.api.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT DISTINCT e FROM Expense e LEFT JOIN FETCH e.splits WHERE e.group.id = :groupId")
    List<Expense> findByGroupId(Long groupId);

    List<Expense> findByGroupIdIn(List<Long> groupIds);
}
