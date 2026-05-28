package com.rachai.api.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rachai.api.dto.userDTOs.UserProfileDTO;
import com.rachai.api.dto.userDTOs.UserResponseDTO;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.mapper.DozerMapper;
import com.rachai.api.model.User;
import com.rachai.api.repository.FriendshipRepository;
import com.rachai.api.repository.UserRepository;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class) ;

    @Autowired
    private UserRepository repository;

    @Autowired
    private FriendshipRepository friendshipRepository;


    // OK
    public List<UserResponseDTO> findAll() {
        logger.info("Finding All Users!");

        List<User> entityList = repository.findAll();

        return DozerMapper.parseListObjects(entityList, UserResponseDTO.class);
    }


    // OK
    public Optional<UserProfileDTO> findProfileById(Long id) {
        logger.info("Finding profile for user ID: {}", id);

        return repository.findById(id).map(user -> DozerMapper.parseObject(user, UserProfileDTO.class));
    }


    // OK
    public UserProfileDTO updateProfile(String email, UserProfileDTO dto) {
        logger.info("Updating profile for email: {}", email);

        User entity = repository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("There's no user with this email"));
        
        DozerMapper.mergeObject(dto, entity);

        User updatedUser = repository.save(entity);

        return DozerMapper.parseObject(updatedUser, UserProfileDTO.class);
    }

    // Para analisar...
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    // OK
    public Optional<UserProfileDTO> findProfileByEmail(String email) {
        logger.info("Finding profile for email: {}", email);

        return repository.findByEmail(email).map(user -> DozerMapper.parseObject(user, UserProfileDTO.class));
    }

    // OK
    public List<UserResponseDTO> searchUsers(String query) {
        logger.info("Searching users with query: {}", query);

        List<User> users = repository.findByFirstNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);

        return DozerMapper.parseListObjects(users, UserResponseDTO.class);
    }
}