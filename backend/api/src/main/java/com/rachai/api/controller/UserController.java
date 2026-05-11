package com.rachai.api.controller;

import com.rachai.api.dto.FriendshipDTO;
import com.rachai.api.service.FriendshipService;
import com.rachai.api.dto.UserProfileDTO;
import com.rachai.api.dto.UserUpdateDTO;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.model.User;
import com.rachai.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

    @GetMapping
    public List<User> listAll() {
        return userService.findAll();
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> showProfile(Authentication authentication) {
        String email = authentication.getName();
        UserProfileDTO profile = userService.findProfileByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> update(Authentication authentication, @RequestBody UserUpdateDTO updateData) {
        String email = authentication.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        UserProfileDTO updatedProfile = userService.updateProfile(user.getId(), updateData);

        return ResponseEntity.ok(updatedProfile);
    }

    // Endpoint dentro de User para adicionar amigo
    @PostMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<FriendshipDTO> addFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        return ResponseEntity.ok(friendshipService.addFriend(userId, friendId));
    }

    // Endpoint dentro de User para listar amigos
    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<FriendshipDTO>> listFriends(@PathVariable Long userId) {
        return ResponseEntity.ok(friendshipService.listFriends(userId));
    }

}
