package com.rachai.api.service;

import com.rachai.api.model.Usuario;
import com.rachai.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario cadastrar(Usuario user) {
        return usuarioRepository.save(user);
    }

}
