package com.rachai.api.controller;

import com.rachai.api.model.Usuario;
import com.rachai.api.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    public Usuario criarConta(@RequestBody Usuario usuario) {
        return authService.cadastrar(usuario);
    }
}
