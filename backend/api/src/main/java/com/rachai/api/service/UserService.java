package com.rachai.api.service;

import com.rachai.api.dto.UserProfileDTO;
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

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public Optional<UserProfileDTO> findProfileById(Long id){
        return userRepository.findById(id).map(user -> new UserProfileDTO(user.getName(), user.getEmail(), user.getImageUrl(), user.getBio()));
    }


    
}
