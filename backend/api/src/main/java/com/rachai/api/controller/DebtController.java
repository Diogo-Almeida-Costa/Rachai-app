package com.rachai.api.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rachai.api.dto.TabscannerResponseDTO;
import com.rachai.api.dto.debtDTOs.DebtResponseDTO;
import com.rachai.api.model.User;
import com.rachai.api.service.DebtService;
import com.rachai.api.service.TabscannerService;

@RestController
@RequestMapping("/rachai/debts")
public class DebtController {

    private static final Logger logger = LoggerFactory.getLogger(DebtController.class);

    @Autowired
    private DebtService debtService;

    @Autowired
    private TabscannerService tabscannerService;

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping("/group/{groupId}/calculate")
    public ResponseEntity<List<DebtResponseDTO>> calculateDebts(@PathVariable Long groupId) {
        logger.info("HTTP POST request received to calculate and simplify debts for group ID: {}", groupId);
        List<DebtResponseDTO> debts = debtService.calculateAndSimplifyDebts(groupId);
        return ResponseEntity.ok(debts);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<DebtResponseDTO>> getDebtsByGroup(@PathVariable Long groupId) {
        logger.info("HTTP GET request received to fetch simplified debts for group ID: {}", groupId);
        List<DebtResponseDTO> debts = debtService.getDebtsByGroup(groupId);
        return ResponseEntity.ok(debts);
    }

    @PutMapping("/{debtId}/settle")
    public ResponseEntity<DebtResponseDTO> settleDebt(@PathVariable Long debtId) {
        User requester = getAuthenticatedUser();
        logger.info("HTTP PUT request received to settle debt ID: {} by User ID: {}", debtId, requester.getId());
        
        DebtResponseDTO settledDebt = debtService.settleDebt(debtId, requester.getId());
        return ResponseEntity.ok(settledDebt);
    }

    @PostMapping("/process-invoice")
    public ResponseEntity<?> processInvoice(@RequestParam("file") MultipartFile file) throws InterruptedException {
        logger.info("HTTP POST request received to process invoice via Tabscanner API");
        
        TabscannerResponseDTO initialResponse = tabscannerService.processReceipt(file);
        String token = tabscannerService.extractToken(initialResponse);

        Thread.sleep(3000);

        TabscannerResponseDTO finalResult = tabscannerService.searchResult(token);

        if (finalResult != null && finalResult.getResult() != null) {
            return ResponseEntity.ok(finalResult.getResult().getLineItems());
        }
        
        return ResponseEntity.ok(finalResult);
    }
}