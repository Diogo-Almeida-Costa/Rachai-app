package com.taskflow.controller;

import com.framework.extension.user.IUser;
import com.taskflow.service.TaskFlowFrameworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/framework/tasks")
public class FrameworkTaskController {

    @Autowired
    private TaskFlowFrameworkService taskFlowFrameworkService;

    @PostMapping("/process")
    public ResponseEntity<Map<IUser, BigDecimal>> processarListaDeTarefas(
            @RequestParam("file") MultipartFile file,
            @RequestParam("collaboratorIds") List<Long> collaboratorIds) {

        Map<IUser, BigDecimal> resultado = taskFlowFrameworkService.processarListaDeTarefas(file, collaboratorIds);
        return ResponseEntity.ok(resultado);
    }
}
