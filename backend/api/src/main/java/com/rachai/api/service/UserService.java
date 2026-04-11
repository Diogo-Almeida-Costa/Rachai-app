package com.rachai.api.service;

import com.rachai.api.dto.UserProfileDTO;
import com.rachai.api.dto.UserUpdateDTO;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.model.User;
import com.rachai.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<UserProfileDTO> findProfileById(Long id) {
        return userRepository.findById(id)
                .map(user -> new UserProfileDTO(user.getName(), user.getEmail(), user.getImageUrl(), user.getBio()));
    }

    public UserProfileDTO updateProfile(Long id, UserUpdateDTO updateData) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        user.setName(updateData.getName());
        user.setImageUrl(updateData.getImageUrl());
        user.setBio(updateData.getBio());

        User updatedUser = userRepository.save(user);

        return new UserProfileDTO(updatedUser.getName(), updatedUser.getEmail(), updatedUser.getImageUrl(),
                updatedUser.getBio());
    }

}
