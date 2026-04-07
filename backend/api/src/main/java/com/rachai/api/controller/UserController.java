package com.rachai.api.controller;

import com.rachai.api.dto.UserProfileDTO;
import com.rachai.api.dto.UserUpdateDTO;
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

    @GetMapping
    public List<User> listAll() {
        return userService.findAll();
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> showProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        return userService.findProfileById(user.getId()).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> update(Authentication authentication, @RequestBody UserUpdateDTO updateData) {
        User user = (User) authentication.getPrincipal();
        UserProfileDTO updatedProfile = userService.updateProfile(user.getId(), updateData);
        return ResponseEntity.ok(updatedProfile);
    }
}
