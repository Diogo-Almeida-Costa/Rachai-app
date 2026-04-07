package com.rachai.api.service;

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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Senha inválida");
        }

        return jwtService.generateToken(email);
    }

    public User register(User user) {
        return userRepository.save(user);
    }
}
