package com.rachai.api.controller;

import com.rachai.api.dto.FriendshipDTO;
import com.rachai.api.service.FriendshipService;
import com.rachai.api.dto.UserProfileDTO;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.model.User;
import com.rachai.api.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import java.util.List;

import org.springframework.http.MediaType;

@RestController
@RequestMapping("rachai/users")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private FriendshipService friendshipService;

    @RequestMapping(method = RequestMethod.GET , produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> listAll() {
        return service.findAll();
    }

    @RequestMapping(value = "/me" , method = RequestMethod.PUT , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserProfileDTO> update(Authentication authentication, @RequestBody User user) {
        String email = authentication.getName();

        UserProfileDTO updatedProfile = service.updateProfile(email , user);

        return ResponseEntity.ok(updatedProfile);
    }


    @RequestMapping(value = "/me" , method = RequestMethod.GET , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserProfileDTO> showProfile(Authentication authentication) {
        String email = authentication.getName();

        UserProfileDTO myProfile = service.findProfileByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado"));

        return ResponseEntity.ok(myProfile);
    }

    
    @PostMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<FriendshipDTO> addFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        return ResponseEntity.ok(friendshipService.addFriend(userId, friendId));
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<FriendshipDTO>> listFriends(@PathVariable Long userId) {
        return ResponseEntity.ok(friendshipService.listFriends(userId));
    }

}
