package com.taskflow.service;

import com.taskflow.model.AppUser;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Armazenamento em memória dos usuários (gestores) desta instância, seguindo
 * o mesmo padrão de simplicidade adotado no restante do TaskFlow (sem banco
 * de dados).
 */
@Service
public class AppUserService {

    private final Map<String, AppUser> usersByEmail = new ConcurrentHashMap<>();
    private final AtomicLong userIds = new AtomicLong(1);

    public AppUser salvar(AppUser user) {
        if (user.getId() == null) {
            user.setId(userIds.getAndIncrement());
        }
        usersByEmail.put(user.getEmail().toLowerCase(), user);
        return user;
    }

    public Optional<AppUser> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        return Optional.ofNullable(usersByEmail.get(email.toLowerCase()));
    }
}
