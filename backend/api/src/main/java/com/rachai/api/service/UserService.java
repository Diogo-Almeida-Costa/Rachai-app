package com.rachai.api.service;

import com.rachai.api.dto.UserProfileDTO;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.model.User;
import com.rachai.api.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class) ;

    @Autowired
    private UserRepository repository;

    public List<User> findAll() {
        logger.info("Finding All Users!");
        return repository.findAll();
    }

    public User findProfileById(Long id) {
        logger.info("Searching One Person by Id");
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No records found for this ID"));
    }

    public User updateProfile(String email , User user) {
        logger.info("Updating the profile with ID: {} and name {} {}" , user.getId() , user.getFirstName(), user.getLastName());
        User entity = repository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("There's no user with this email"));

        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setImageUrl(user.getImageUrl());
        entity.setBio(user.getBio());

        return repository.save(entity);
    }

    public User findByEmail(String email) {
        logger.info("Searching User with email: {}" , email);
        return repository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("There's no User with this email"));
    }

    public UserProfileDTO findProfileByEmail(String email) {
        logger.info("Searching User's Profile with email: {}" , email);

        User user = repository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("There's no User's Profile with this email"));

        return new UserProfileDTO(user.getFirstName(), user.getLastName(), user.getEmail(), user.getImageUrl(),user.getBio());
    }
}
