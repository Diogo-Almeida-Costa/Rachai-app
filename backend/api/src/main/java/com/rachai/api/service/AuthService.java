package com.rachai.api.service;

import com.rachai.api.model.User;
import com.rachai.api.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository usuarioRepository;

    public AuthService(UserRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public User register(User user) {
        return usuarioRepository.save(user);
    }

}
