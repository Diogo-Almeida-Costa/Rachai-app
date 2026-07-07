package com.rachai.repository;

import com.rachai.model.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByGroupId(Long groupId);

    List<Debt> findByGroupIdAndSettled(Long groupId, boolean settled);

    @Transactional
    void deleteByGroupIdAndSettled(Long groupId, boolean settled);
}
