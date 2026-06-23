package com.rachai.api.controller;

import com.rachai.api.service.RachAIService;
import com.rachai.framework.core.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/framework/expenses")
public class FrameworkExpenseController {

    @Autowired
    private RachAIService rachAIService;

    @PostMapping("/process")
    public ResponseEntity<Map<User, BigDecimal>> processExpense(
            @RequestParam("file") MultipartFile file,
            @RequestParam("groupId") Long groupId) {
        
        Map<User, BigDecimal> result = rachAIService.processExpenseWithFramework(file, groupId);
        return ResponseEntity.ok(result);
    }
}
