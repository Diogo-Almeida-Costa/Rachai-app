package com.rachai.api.controller;

import java.util.List;

import org.slf4j.Logger; // Adicionado o import do DTO correto
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rachai.api.dto.friendshipDTOs.FriendshipDTO;
import com.rachai.api.dto.friendshipDTOs.FriendshipResponseDTO;
import com.rachai.api.dto.userDTOs.UserProfileDTO;
import com.rachai.api.dto.userDTOs.UserResponseDTO;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.model.User;
import com.rachai.api.service.FriendshipService;
import com.rachai.api.service.UserService;

@RestController
@RequestMapping("/rachai/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService service;

    @Autowired
    private FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listAll() {
        logger.info("HTTP GET request received to list all application users");

        List<UserResponseDTO> users = service.findAll();
        
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> showProfile(Authentication authentication) {
        String email = authentication.getName();
        logger.info("HTTP GET request received for authenticated user profile: {}", email);

        UserProfileDTO myProfile = service.findProfileByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado"));

        return ResponseEntity.ok(myProfile);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> update(Authentication authentication, @RequestBody UserProfileDTO user) {
        String email = authentication.getName();
        logger.info("HTTP PUT request received to update profile for user: {}", email);

        UserProfileDTO updatedProfile = service.updateProfile(email, user);

        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/friends/{friendId}")
    public ResponseEntity<FriendshipDTO> addFriend(Authentication authentication, @PathVariable Long friendId) {
        User currentUser = (User) authentication.getPrincipal();
        logger.info("User ID: {} is attempting to add Friend ID: {}", currentUser.getId(), friendId);

        String name = currentUser.getFirstName();

        FriendshipDTO response = friendshipService.addFriend(currentUser.getId(), friendId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/friends")
    public ResponseEntity<List<FriendshipResponseDTO>> listFriends(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        logger.info("HTTP GET request received to list friends for user ID: {}", currentUser.getId());

        // 2. Mude o tipo da variável que recebe o resultado
        List<FriendshipResponseDTO> friends = friendshipService.listFriends(currentUser.getId());
        return ResponseEntity.ok(friends);
    }
}