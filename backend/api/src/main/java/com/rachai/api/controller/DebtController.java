package com.rachai.api.controller;

import com.rachai.api.model.Debt;
import com.rachai.api.service.DebtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.MediaType;

@RestController
@RequestMapping("/rachai/debts")
public class DebtController {

    @Autowired
    private DebtService debtService;

    @RequestMapping(value = "/group/{groupId}/calculate" , method = RequestMethod.POST , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Debt>> calculateDebts(@PathVariable Long groupId) {
        List<Debt> debts = debtService.calculateAndSimplifyDebts(groupId);
        return new ResponseEntity<>(debts, HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}" , method = RequestMethod.GET , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Debt>> getDebtsByGroup(@PathVariable Long groupId) {
        List<Debt> debts = debtService.getDebtsByGroup(groupId);
        return new ResponseEntity<>(debts, HttpStatus.OK);
    }

    @RequestMapping(value = "/{debtId}/settle" , method = RequestMethod.PUT , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Debt> settleDebt(@PathVariable Long debtId) {
        Debt settledDebt = debtService.settleDebt(debtId);
        return new ResponseEntity<>(settledDebt, HttpStatus.OK);
    }
}
