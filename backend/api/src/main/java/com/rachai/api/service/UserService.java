package com.rachai.api.service;

import com.rachai.api.dto.UserProfileDTO;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.model.Friendship;
import com.rachai.api.model.User;
import com.rachai.api.repository.FriendshipRepository;
import com.rachai.api.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class) ;

    @Autowired
    private UserRepository repository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    public List<User> findAll() {
        logger.info("Finding All Users!");
        return repository.findAll();
    }

    public Optional<UserProfileDTO> findProfileById(Long id) {
        return repository.findById(id).map(user -> new UserProfileDTO(user.getFirstName(), user.getLastName(), user.getEmail(), user.getImageUrl(), user.getBio()));
    }

    public UserProfileDTO updateProfile(String email, User user) {
        logger.info("Updating profile for email: {}", email);
        User entity = repository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("There's no user with this email"));

        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setImageUrl(user.getImageUrl());
        entity.setBio(user.getBio());

        User updatedUser = repository.save(entity);

        return new UserProfileDTO(updatedUser.getFirstName(), updatedUser.getLastName(), updatedUser.getEmail(), updatedUser.getImageUrl(), updatedUser.getBio());
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Optional<UserProfileDTO> findProfileByEmail(String email) {
        return repository.findByEmail(email).map(user -> new UserProfileDTO(user.getFirstName(), user.getLastName(), user.getEmail(), user.getImageUrl(), user.getBio()));
    }

    public List<User> searchUsers(String query) {
        return repository.findByFirstNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    public void addFriend(User user, Long friendId) {
        User friend = repository.findById(friendId).orElseThrow(() -> new ResourceNotFoundException("Amigo não encontrado"));

        if (!friendshipRepository.existsByUserAndFriend(user, friend)) {
            Friendship friendship = new Friendship(user, friend);
            friendshipRepository.save(friendship);
        }
    }

    public List<User> listFriends(User user) {
        return friendshipRepository.findByUser(user).stream().map(Friendship::getFriend).toList();
    }

}