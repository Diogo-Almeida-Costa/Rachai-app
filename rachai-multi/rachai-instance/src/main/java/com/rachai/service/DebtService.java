package com.rachai.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rachai.dto.debtDTOs.DebtResponseDTO;
import com.framework.core.exception.BusinessException;
import com.framework.core.exception.ResourceNotFoundException;
import com.rachai.mapper.DozerMapper;
import com.rachai.model.Debt;
import com.rachai.model.Expense;
import com.rachai.model.ExpenseSplit;
import com.rachai.model.Group;
import com.rachai.model.User;
import com.rachai.repository.DebtRepository;
import com.rachai.repository.ExpenseRepository;
import com.rachai.repository.GroupRepository;

@Service
public class DebtService {

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupRepository groupRepository;

    private static final Logger logger = LoggerFactory.getLogger(DebtService.class);

    //OK
    @Transactional
    public List<DebtResponseDTO> calculateAndSimplifyDebts(Long groupId) {
        logger.info("Attempting to calculate and simplify debts for group ID: {}", groupId);
        
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        Set<User> members = group.getMembers();
        
        if (members.isEmpty()) {
            throw new BusinessException("Group has no members");
        }

        Map<User, BigDecimal> balances = new HashMap<>();
        for (User member : members) {
            balances.put(member, BigDecimal.ZERO);
        }

        for (Expense expense : expenses) {
            User payer = expense.getPayer();
            BigDecimal amount = expense.getAmount();

            balances.put(payer, balances.get(payer).add(amount));

            for (ExpenseSplit split : expense.getSplits()) {
                User debtor = split.getDebtor();
                BigDecimal share = split.getShare();

                if (balances.containsKey(debtor)) {
                    balances.put(debtor, balances.get(debtor).subtract(share));
                }
            }
        }

        List<Debt> settledDebts = debtRepository.findByGroupIdAndSettled(groupId, true);
        for (Debt sd : settledDebts) {
            balances.put(sd.getDebtor(), balances.get(sd.getDebtor()).add(sd.getAmount()));
            balances.put(sd.getCreditor(), balances.get(sd.getCreditor()).subtract(sd.getAmount()));
        }

        List<UserBalance> creditors = new ArrayList<>();
        List<UserBalance> debtors = new ArrayList<>();

        for (Map.Entry<User, BigDecimal> entry : balances.entrySet()) {
            BigDecimal balance = entry.getValue();
            if (balance.compareTo(new BigDecimal("0.01")) > 0) {
                creditors.add(new UserBalance(entry.getKey(), balance));
            } else if (balance.compareTo(new BigDecimal("-0.01")) < 0) {
                debtors.add(new UserBalance(entry.getKey(), balance.abs()));
            }
        }

        List<Debt> simplifiedDebts = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            UserBalance debtor = debtors.get(i);
            UserBalance creditor = creditors.get(j);

            BigDecimal amountToPay = debtor.balance.min(creditor.balance);

            Debt debt = new Debt();
            debt.setDebtor(debtor.user);
            debt.setCreditor(creditor.user);
            debt.setAmount(amountToPay);
            debt.setGroup(group);
            debt.setSettled(false);
            simplifiedDebts.add(debt);

            debtor.balance = debtor.balance.subtract(amountToPay);
            creditor.balance = creditor.balance.subtract(amountToPay);

            if (debtor.balance.compareTo(BigDecimal.ZERO) <= 0) i++;
            if (creditor.balance.compareTo(BigDecimal.ZERO) <= 0) j++;
        }

        debtRepository.deleteByGroupIdAndSettled(groupId, false);
        List<Debt> savedDebts = debtRepository.saveAll(simplifiedDebts);

        logger.info("Successfully generated {} simplified debts for group ID: {}", savedDebts.size(), groupId);


        return savedDebts.stream().map(debt -> DozerMapper.parseObject(debt, DebtResponseDTO.class)).toList();
    }

    //OK
    @Transactional(readOnly = true)
    public List<DebtResponseDTO> getDebtsByGroup(Long groupId) {
        logger.info("Attempting to find debts for group ID: {}", groupId);

        groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group not found"));


        return debtRepository.findByGroupId(groupId).stream().map(debt -> DozerMapper.parseObject(debt, DebtResponseDTO.class)).toList();
    }

    @Transactional
    public DebtResponseDTO settleDebt(Long debtId, Long authenticatedUserId) {
        logger.info("User ID: {} is attempting to settle debt ID: {}", authenticatedUserId, debtId);

        Debt debt = findDebtOrThrow(debtId);

        if (!debt.getDebtor().getId().equals(authenticatedUserId)) {
            logger.warn("Security alert: User ID: {} tried to settle debt ID: {} but is not the debtor!", authenticatedUserId, debtId);
            throw new IllegalStateException("Você não tem permissão para quitar esta dívida, pois ela pertence a outro usuário.");
        }

        if (debt.isSettled()) {
            throw new BusinessException("Debt already settled");
        }

        debt.setSettled(true);
        
        Debt settledDebt = debtRepository.save(debt);
        logger.info("Debt ID: {} successfully settled by debtor ID: {}", debtId, authenticatedUserId);
        
        return DozerMapper.parseObject(settledDebt, DebtResponseDTO.class);
    }

    private static class UserBalance {
        User user;
        BigDecimal balance;

        UserBalance(User user, BigDecimal balance) {
            this.user = user;
            this.balance = balance;
        }
    }

    //Método auxiliar
    private Debt findDebtOrThrow(Long debtId) {
        return debtRepository.findById(debtId).orElseThrow(() -> new ResourceNotFoundException("Debt not found"));
    }
}
