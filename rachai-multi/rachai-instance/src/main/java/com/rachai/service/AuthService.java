package com.rachai.service;

import com.rachai.dto.userDTOs.UserResponseDTO;
import com.framework.core.exception.BusinessException;
import com.rachai.mapper.DozerMapper;
import com.rachai.model.User;
import com.rachai.repository.UserRepository;
import com.rachai.security.JwtService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rachai.dto.authenticationDTOs.RegisterRequestDTO;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(String email, String password) {
        logger.info("Authentication attempt for email: {}", email);


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Authentication failed: email {} not found", email);
                    return new BusinessException("E-mail ou senha inválidos");
                });


        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Authentication failed: wrong password for email {}", email);
            throw new BusinessException("E-mail ou senha inválidos");
        }

        logger.info("User {} successfully authenticated", email);
        return jwtService.generateToken(email);
    }

    @Transactional
    public UserResponseDTO register(RegisterRequestDTO dto) {
        logger.info("Attempting to register a new user with email: {}", dto.getEmail());

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new BusinessException("Email é obrigatório");
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BusinessException("Senha é obrigatória");
        }

        if (dto.getPassword().length() < 6) {
            throw new BusinessException("A senha deve conter pelo menos 6 caracteres");
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            logger.warn("Registration failed: email {} already taken", dto.getEmail());
            throw new BusinessException("Já existe um usuário com esse email");
        }

        User user = DozerMapper.parseObject(dto, User.class);
        
        String encryptedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encryptedPassword);

        User savedUser = userRepository.save(user);
        logger.info("User successfully registered with ID: {}", savedUser.getId());
        
        return DozerMapper.parseObject(savedUser, UserResponseDTO.class);
    }
}