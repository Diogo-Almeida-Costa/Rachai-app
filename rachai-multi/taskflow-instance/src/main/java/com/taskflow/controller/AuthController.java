package com.taskflow.controller;

import com.taskflow.dto.authenticationDTOs.AuthenticationRequestDTO;
import com.taskflow.dto.authenticationDTOs.RegisterRequestDTO;
import com.taskflow.dto.authenticationDTOs.UserResponseDTO;
import com.taskflow.model.AppUser;
import com.taskflow.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/taskflow/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthenticationRequestDTO dto) {
        String token = authService.login(dto.email(), dto.password());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(new UserResponseDTO(user.getId(), user.getName(), user.getEmail()));
    }
}
