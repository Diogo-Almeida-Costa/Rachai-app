package com.rachai.api.repository;

import com.rachai.api.model.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByGroupId(Long groupId);
    List<Debt> findByGroupIdAndSettled(Long groupId, boolean settled);
    void deleteByGroupId(Long groupId);
}
