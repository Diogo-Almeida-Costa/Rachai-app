package com.rachai.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.rachai.dto.expenseDTOs.ExpenseRequestDTO;
import com.rachai.dto.expenseDTOs.ExpenseResponseDTO;
import com.rachai.model.User;
import com.rachai.service.ExpenseService;

@RestController
@RequestMapping("/rachai/expenses")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    @Autowired
    private ExpenseService expenseService;

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(@RequestBody ExpenseRequestDTO expenseDTO) {
        logger.info("HTTP POST request received to create expense: '{}'", expenseDTO.getDescription());
        ExpenseResponseDTO createdExpense = expenseService.createExpense(expenseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByGroup(@PathVariable Long groupId) {
        logger.info("HTTP GET request received to fetch expenses for group ID: {}", groupId);
        List<ExpenseResponseDTO> expenses = expenseService.getExpensesByGroup(groupId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(@PathVariable Long expenseId) {
        logger.info("HTTP GET request received for expense ID: {}", expenseId);
        ExpenseResponseDTO expense = expenseService.getExpenseById(expenseId);
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(@PathVariable Long expenseId, @RequestBody ExpenseRequestDTO dto) {
        User requester = getAuthenticatedUser();
        logger.info("HTTP PUT request received to update expense ID: {} by User ID: {}", expenseId, requester.getId());
        
        ExpenseResponseDTO updatedExpense = expenseService.updateExpense(expenseId, dto, requester.getId());
        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
        User requester = getAuthenticatedUser();
        logger.info("HTTP DELETE request received for expense ID: {} by User ID: {}", expenseId, requester.getId());
        
        expenseService.deleteExpense(expenseId, requester.getId());
        return ResponseEntity.noContent().build();
    }
}