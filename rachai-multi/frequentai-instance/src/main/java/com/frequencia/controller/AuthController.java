package com.frequencia.controller;

import com.frequencia.dto.authenticationDTOs.AuthenticationRequestDTO;
import com.frequencia.dto.authenticationDTOs.RegisterRequestDTO;
import com.frequencia.dto.authenticationDTOs.UserResponseDTO;
import com.frequencia.model.AppUser;
import com.frequencia.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/frequencia/auth")
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
