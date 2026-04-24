package com.rachai.api.controller;

import com.rachai.api.model.Debt;
import com.rachai.api.model.User;
import com.rachai.api.service.DebtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debts")
public class DebtController {

    @Autowired
    private DebtService debtService;

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping
    public ResponseEntity<?> createDebt(@RequestBody Debt debt) {

        User authenticatedUser = getAuthenticatedUser();

        // 2. Define que o dono do token é quem tem o crédito a receber
        debt.setCreditor(authenticatedUser);

        if (debt.getDebtor().getId().equals(authenticatedUser.getId())) {

            return ResponseEntity.badRequest().body("Você não pode criar uma dívida contra si mesmo.");

        }
                
        Debt newDebt = debtService.save(debt); 
        return new ResponseEntity<>(newDebt, HttpStatus.CREATED);
    }

    @PostMapping("/group/{groupId}/calculate")
    public ResponseEntity<List<Debt>> calculateDebts(@PathVariable Long groupId) {
        List<Debt> debts = debtService.calculateAndSimplifyDebts(groupId);
        return new ResponseEntity<>(debts, HttpStatus.OK);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Debt>> getDebtsByGroup(@PathVariable Long groupId) {
        List<Debt> debts = debtService.getDebtsByGroup(groupId);
        return new ResponseEntity<>(debts, HttpStatus.OK);
    }

    @PutMapping("/{debtId}/settle")
    public ResponseEntity<Debt> settleDebt(@PathVariable Long debtId) {
        Debt settledDebt = debtService.settleDebt(debtId);
        return new ResponseEntity<>(settledDebt, HttpStatus.OK);
    }
}
