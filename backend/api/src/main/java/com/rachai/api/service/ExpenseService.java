package com.rachai.api.service;

import com.rachai.api.dto.ExpenseDTO;
import com.rachai.api.model.Expense;
import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.repository.ExpenseRepository;
import com.rachai.api.repository.GroupRepository;
import com.rachai.api.repository.UserRepository;
import com.rachai.api.exception.BusinessException;
import com.rachai.api.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Expense createExpense(ExpenseDTO expenseDTO) {
        logger.info("Creating Expense!");
        Group group = groupRepository.findById(expenseDTO.getGroupId()).orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        User payer = userRepository.findById(expenseDTO.getPayerId()).orElseThrow(() -> new ResourceNotFoundException("Payer not found"));

        if(!group.getMembers().contains(payer)) {
            throw new BusinessException("Payer is not a member of this group");
        }

        if(expenseDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }

        Expense expense = new Expense();
        expense.setDescription(expenseDTO.getDescription());
        expense.setAmount(expenseDTO.getAmount());
        expense.setPayer(payer);
        expense.setGroup(group);

        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByGroup(Long groupId) {
        logger.info("Searching Expenses by Group");

        groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        return expenseRepository.findByGroupId(groupId);
    }

    public Expense getExpenseById(Long expenseId){
        logger.info("Searching Expense with ID {}", expenseId);

        return expenseRepository.findById(expenseId).orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
    }

    @Transactional
    public Expense updateExpense(Long expenseId, ExpenseDTO dto) {

        logger.info("Updating Expense with ID {}", expenseId);

        Expense expense = expenseRepository.findById(expenseId).orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        Group group = groupRepository.findById(dto.getGroupId()).orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        User payer = userRepository.findById(dto.getPayerId()).orElseThrow(() -> new ResourceNotFoundException("Payer not found"));

        if (!group.getMembers().contains(payer)) {
            throw new BusinessException("Payer is not a member of this group");
        }

        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }

        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setPayer(payer);
        expense.setGroup(group);

        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        logger.info("Deleting Expense with ID {}", expenseId);

        Expense expense = expenseRepository.findById(expenseId).orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        expenseRepository.delete(expense);
    }

}
