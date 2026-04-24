package com.rachai.api.service;

import com.rachai.api.model.Debt;
import com.rachai.api.model.Expense;
import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.repository.DebtRepository;
import com.rachai.api.repository.ExpenseRepository;
import com.rachai.api.repository.GroupRepository;
import com.rachai.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class DebtService {

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Transactional
    public List<Debt> calculateAndSimplifyDebts(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        Set<User> members = group.getMembers();
        
        if (members.isEmpty()) return Collections.emptyList();

        // Calcular o saldo líquido de cada usuário
        Map<User, BigDecimal> balances = new HashMap<>();
        for (User member : members) {
            balances.put(member, BigDecimal.ZERO);
        }

        for (Expense expense : expenses) {
            BigDecimal amount = expense.getAmount();
            BigDecimal share = amount.divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);
            
            User payer = expense.getPayer();
            balances.put(payer, balances.get(payer).add(amount));

            for (User member : members) {
                balances.put(member, balances.get(member).subtract(share));
            }
        }

        // Separar credores e devedores
        List<UserBalance> creditors = new ArrayList<>();
        List<UserBalance> debtors = new ArrayList<>();

        for (Map.Entry<User, BigDecimal> entry : balances.entrySet()) {
            BigDecimal balance = entry.getValue();
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new UserBalance(entry.getKey(), balance));
            } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new UserBalance(entry.getKey(), balance.abs()));
            }
        }

        // Algoritmo de Simplificação
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
            simplifiedDebts.add(debt);

            debtor.balance = debtor.balance.subtract(amountToPay);
            creditor.balance = creditor.balance.subtract(amountToPay);

            if (debtor.balance.compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditor.balance.compareTo(BigDecimal.ZERO) == 0) j++;
        }

        // Salvar as novas dívidas 
        debtRepository.deleteByGroupId(groupId);
        return debtRepository.saveAll(simplifiedDebts);
    }

    public List<Debt> getDebtsByGroup(Long groupId) {
        return debtRepository.findByGroupId(groupId);
    }

    @Transactional
    public Debt settleDebt(Long debtId) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found"));
        debt.setSettled(true);
        return debtRepository.save(debt);
    }

    @Transactional
    public Debt save(Debt debt){
        return debtRepository.save(debt);
    }

    private static class UserBalance {
        User user;
        BigDecimal balance;

        UserBalance(User user, BigDecimal balance) {
            this.user = user;
            this.balance = balance;
        }
    }
}
