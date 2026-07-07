package com.rachai.controller;

import com.rachai.service.RachAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.framework.extension.user.IUser;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/framework/expenses")
public class FrameworkExpenseController {

    @Autowired
    private RachAIService rachAIService;

    @PostMapping("/process")
    public ResponseEntity<Map<IUser, BigDecimal>> processExpense(
            @RequestParam("file") MultipartFile file,
            @RequestParam("groupId") Long groupId) {
        
        Map<IUser, BigDecimal> result = rachAIService.processExpenseWithFramework(file, groupId);


        return ResponseEntity.ok(result);
    }
}
