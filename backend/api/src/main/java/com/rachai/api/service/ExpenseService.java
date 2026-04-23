package com.rachai.api.service;

import com.rachai.api.dto.ExpenseDTO;
import com.rachai.api.model.Expense;
import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.repository.ExpenseRepository;
import com.rachai.api.repository.GroupRepository;
import com.rachai.api.repository.UserRepository;
import com.rachai.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Expense createExpense(ExpenseDTO expenseDTO) {
        Group group = groupRepository.findById(expenseDTO.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        User payer = userRepository.findById(expenseDTO.getPayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Payer not found"));

        Expense expense = new Expense();
        expense.setDescription(expenseDTO.getDescription());
        expense.setAmount(expenseDTO.getAmount());
        expense.setPayer(payer);
        expense.setGroup(group);

        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByGroup(Long groupId) {
        return expenseRepository.findByGroupId(groupId);
    }
}
