package com.rachai.api.controller;

import com.rachai.api.service.AuthService;
import com.rachai.api.dto.authenticationDTOs.AuthenticationRequestDTO;
import com.rachai.api.dto.authenticationDTOs.RegisterRequestDTO; 
import com.rachai.api.dto.userDTOs.UserResponseDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rachai/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> signUp(@RequestBody RegisterRequestDTO registerDto) {
        logger.info("HTTP POST request received for user registration with email: {}", registerDto.getEmail());
        
        UserResponseDTO response = authService.register(registerDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthenticationRequestDTO request) {
        logger.info("HTTP POST request received for user login attempt: {}", request.email());
        
        String token = authService.login(request.email(), request.password());
        
        return ResponseEntity.ok(Map.of("token", token));
    }
}