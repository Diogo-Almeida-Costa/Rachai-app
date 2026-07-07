package com.taskflow.service;

import com.taskflow.dto.authenticationDTOs.RegisterRequestDTO;
import com.taskflow.dto.authenticationDTOs.UserResponseDTO;
import com.taskflow.model.AppUser;
import com.taskflow.security.JwtService;
import com.framework.core.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponseDTO register(RegisterRequestDTO dto) {
        if (dto.email() == null || dto.email().isBlank()) {
            throw new BusinessException("E-mail é obrigatório");
        }
        if (dto.name() == null || dto.name().isBlank()) {
            throw new BusinessException("Nome é obrigatório");
        }
        if (dto.password() == null || dto.password().length() < 6) {
            throw new BusinessException("A senha deve conter pelo menos 6 caracteres");
        }
        if (appUserService.buscarPorEmail(dto.email()).isPresent()) {
            throw new BusinessException("Já existe um usuário com esse e-mail");
        }

        AppUser user = new AppUser(null, dto.name(), dto.email(), passwordEncoder.encode(dto.password()));
        AppUser salvo = appUserService.salvar(user);
        return new UserResponseDTO(salvo.getId(), salvo.getName(), salvo.getEmail());
    }

    public String login(String email, String password) {
        AppUser user = appUserService.buscarPorEmail(email)
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("E-mail ou senha inválidos");
        }

        return jwtService.generateToken(email);
    }
}
