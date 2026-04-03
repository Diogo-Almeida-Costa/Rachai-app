package com.rachai.api.controller;

import com.rachai.api.dto.UserProfileDTO;
import com.rachai.api.model.User;
import com.rachai.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> showProfile(@PathVariable Long id){
        return userService.findProfileById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
}
