package com.rachai.api.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rachai.api.dto.expenseDTOs.ExpenseRequestDTO;
import com.rachai.api.dto.expenseDTOs.ExpenseResponseDTO;
import com.rachai.api.dto.expenseSplitDTOs.ExpenseSplitRequestDTO;
import com.rachai.api.exception.BusinessException;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.mapper.DozerMapper;
import com.rachai.api.model.Expense;
import com.rachai.api.model.ExpenseSplit;
import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.repository.ExpenseRepository;
import com.rachai.api.repository.GroupRepository;
import com.rachai.api.repository.UserRepository;

@Service
public class ExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    //OK
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto) {
        logger.info("Attempting to create expense '{}' with amount {} in group ID: {}", dto.getDescription(), dto.getAmount(), dto.getGroupId());

        Group group = groupRepository.findById(dto.getGroupId()).orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        User payer = userRepository.findById(dto.getPayerId()).orElseThrow(() -> new ResourceNotFoundException("Payer not found"));

        if (!group.getMembers().contains(payer)) {
            throw new BusinessException("Payer is not a member of this group");
        }

        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }

        if (dto.getSplits() == null || dto.getSplits().isEmpty()) {
            throw new BusinessException("Expense must have at least one split");
        }

        Expense expense = new Expense();
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setPayer(payer);
        expense.setGroup(group);


        BigDecimal totalSplitsSum = BigDecimal.ZERO;

        for (ExpenseSplitRequestDTO splitDTO : dto.getSplits()) {

            User debtor = userRepository.findById(splitDTO.getDebtorId()).orElseThrow(() -> new ResourceNotFoundException("Debtor user not found"));

            if (!group.getMembers().contains(debtor)) {
                throw new BusinessException("Debtor with ID " + debtor.getId() + " is not a member of this group");
            }

            if (splitDTO.getShare().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Each split share must be greater than zero");
            }

            totalSplitsSum = totalSplitsSum.add(splitDTO.getShare());

            ExpenseSplit split = new ExpenseSplit();
            split.setDebtor(debtor);
            split.setShare(splitDTO.getShare());
            
            expense.addSplit(split);
        }

        if (totalSplitsSum.compareTo(expense.getAmount()) != 0) {
            throw new BusinessException("The sum of all splits (" + totalSplitsSum + ") does not match the total expense amount (" + expense.getAmount() + ")");
        }

        Expense savedExpense = expenseRepository.save(expense);

        return DozerMapper.parseObject(savedExpense, ExpenseResponseDTO.class);
    }

    //OK
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByGroup(Long groupId) {
        logger.info("Attempting to find expenses for group ID: {}", groupId);

        groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        return expenseRepository.findByGroupId(groupId).stream().map(expense -> DozerMapper.parseObject(expense, ExpenseResponseDTO.class)).toList();
    }

    //OK
    @Transactional(readOnly = true)
    public ExpenseResponseDTO getExpenseById(Long expenseId) {
        logger.info("Attempting to find expense by ID: {}", expenseId);

        Expense expense = findExpenseOrThrow(expenseId);

        return DozerMapper.parseObject(expense, ExpenseResponseDTO.class);
    }

    //OK
    @Transactional
    public ExpenseResponseDTO updateExpense(Long expenseId, ExpenseRequestDTO dto, Long authenticatedUserId) {
        logger.info("User ID: {} is attempting to update expense ID: {}", authenticatedUserId, expenseId);

        Expense expense = findExpenseOrThrow(expenseId);

        boolean isPayer = expense.getPayer().getId().equals(authenticatedUserId);
        boolean isGroupOwner = expense.getGroup().getOwner().getId().equals(authenticatedUserId);

        if (!isPayer && !isGroupOwner) {
            logger.warn("Security alert: User ID: {} tried to update expense ID: {} without proper permissions!", authenticatedUserId, expenseId);
            throw new IllegalStateException("Você não tem permissão para alterar esta despesa.");
        }

        Group group = groupRepository.findById(dto.getGroupId()).orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        User newPayer = userRepository.findById(dto.getPayerId()).orElseThrow(() -> new ResourceNotFoundException("Payer not found"));

        if (!group.getMembers().contains(newPayer)) {
            throw new BusinessException("Payer is not a member of this group");
        }

        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }

        if (dto.getSplits() == null || dto.getSplits().isEmpty()) {
            throw new BusinessException("Expense must have at least one split");
        }

        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setPayer(newPayer);
        expense.setGroup(group);

        expense.getSplits().clear();

        BigDecimal totalSplitsSum = BigDecimal.ZERO;

        for (ExpenseSplitRequestDTO splitDTO : dto.getSplits()) {
            User debtor = userRepository.findById(splitDTO.getDebtorId()).orElseThrow(() -> new ResourceNotFoundException("Debtor user not found"));

            if (!group.getMembers().contains(debtor)) {
                throw new BusinessException("Debtor with ID " + debtor.getId() + " is not a member of this group");
            }

            if (splitDTO.getShare().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Each split share must be greater than zero");
            }

            totalSplitsSum = totalSplitsSum.add(splitDTO.getShare());

            ExpenseSplit split = new ExpenseSplit();
            split.setDebtor(debtor);
            split.setShare(splitDTO.getShare());
            
            expense.addSplit(split);
        }

        if (totalSplitsSum.compareTo(expense.getAmount()) != 0) {
            throw new BusinessException("The sum of all splits (" + totalSplitsSum + ") does not match the total expense amount (" + expense.getAmount() + ")");
        }

        Expense updatedExpense = expenseRepository.save(expense);

        return DozerMapper.parseObject(updatedExpense, ExpenseResponseDTO.class);
    }

    @Transactional
    public void deleteExpense(Long expenseId, Long authenticatedUserId) {
        logger.info("User ID: {} is attempting to delete expense ID: {}", authenticatedUserId, expenseId);

        Expense expense = findExpenseOrThrow(expenseId);

        boolean isPayer = expense.getPayer().getId().equals(authenticatedUserId);
        boolean isGroupOwner = expense.getGroup().getOwner().getId().equals(authenticatedUserId);

        if (!isPayer && !isGroupOwner) {
            logger.warn("Security alert: User ID: {} tried to delete expense ID: {} without proper permissions!", authenticatedUserId, expenseId);
            throw new IllegalStateException("Você não tem permissão para deletar esta despesa.");
        }

        expenseRepository.delete(expense);
        
        logger.info("Expense with ID: {} successfully deleted by user ID: {}", expenseId, authenticatedUserId);
    }

    //Método auxiliar 
    private Expense findExpenseOrThrow(Long expenseId) {
        return expenseRepository.findById(expenseId).orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
    }

}
