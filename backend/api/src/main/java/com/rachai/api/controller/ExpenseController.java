package com.rachai.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.rachai.api.dto.ExpenseDTO;
import com.rachai.api.model.Expense;
import com.rachai.api.service.ExpenseService;

@RestController
@RequestMapping("/rachai/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @RequestMapping(method = RequestMethod.POST , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Expense> createExpense(@RequestBody ExpenseDTO expenseDTO) {
        Expense createdExpense = expenseService.createExpense(expenseDTO);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/group/{groupId}" , method = RequestMethod.GET , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Expense>> getExpensesByGroup(@PathVariable Long groupId) {
        List<Expense> expenses = expenseService.getExpensesByGroup(groupId);
        return new ResponseEntity<>(expenses, HttpStatus.OK);
    }

    @RequestMapping(value = "/{expenseId}" , method = RequestMethod.GET , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long expenseId) {
        Expense expense = expenseService.getExpenseById(expenseId);

        return ResponseEntity.ok(expense);
    }

    @RequestMapping(value = "/{expenseId}" , method = RequestMethod.PUT , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Expense> updateExpense(@PathVariable Long expenseId, @RequestBody ExpenseDTO dto) {
        Expense updatedExpense = expenseService.updateExpense(expenseId, dto);

        return ResponseEntity.ok(updatedExpense);
    }

    @RequestMapping(value = "/{expenseId}" , method = RequestMethod.DELETE , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);

        return ResponseEntity.noContent().build();
    }
}
