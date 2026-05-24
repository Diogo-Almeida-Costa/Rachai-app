package com.rachai.api.service;

import com.rachai.api.exception.BusinessException;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.model.User;
import com.rachai.api.repository.UserRepository;
import com.rachai.api.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!user.getPassword().equals(password)) {
            throw new BusinessException("Senha inválida");
        }

        return jwtService.generateToken(email);
    }

    public User register(User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BusinessException("Email é obrigatório");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BusinessException("Senha é obrigatória");
        }

        if (user.getPassword().length() < 6) {
            throw new BusinessException("A senha deve conter pelo menos 6 caracteres");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BusinessException("Já existe um usuário com esse email");
        }

        return userRepository.save(user);
    }
}
