package com.rachai.api.controller;

import com.rachai.api.model.Debt;
import com.rachai.api.service.DebtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debts")
public class DebtController {

    @Autowired
    private DebtService debtService;

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
