package com.rachai.api.service;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rachai.api.dto.friendshipDTOs.FriendshipDTO;
import com.rachai.api.dto.friendshipDTOs.FriendshipResponseDTO;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.mapper.DozerMapper;
import com.rachai.api.model.Friendship;
import com.rachai.api.model.User;
import com.rachai.api.repository.FriendshipRepository;
import com.rachai.api.repository.UserRepository;

@Service
public class FriendshipService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class) ;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    // OK
    @Transactional
    public FriendshipDTO addFriend(Long userId, Long friendId) {
        logger.info("Attempting to create friendship. User ID: {} adding Friend ID: {}", userId, friendId);

        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Você não pode adicionar a si mesmo.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        User friend = userRepository.findById(friendId).orElseThrow(() -> new ResourceNotFoundException("Amigo não encontrado"));

        if (friendshipRepository.existsByUserAndFriend(user, friend)) {
            throw new IllegalStateException("Vocês já são amigos.");
        }

        Friendship friendship = new Friendship (user, friend);
        Friendship saved = friendshipRepository.save(friendship);

        return DozerMapper.parseObject(saved, FriendshipDTO.class);
    }

    // OK
    public List<FriendshipResponseDTO> listFriends(Long userId) {
        logger.info("Attempting to list friends for user ID: {}", userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        return friendshipRepository.findByUser(user).stream().map(friendship -> DozerMapper.parseObject(friendship, FriendshipResponseDTO.class)).toList();
    }

    // OK
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        logger.info("Attempting to remove friendship between User ID: {} and Friend ID: {}", userId, friendId);

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
                
        User friend = userRepository.findById(friendId).orElseThrow(() -> new ResourceNotFoundException("Amigo não encontrado"));

        if (!friendshipRepository.existsByUserAndFriend(user, friend)) {
            throw new ResourceNotFoundException("Vínculo de amizade não encontrado entre esses usuários.");
        }

        friendshipRepository.deleteByUserAndFriendId(userId, friendId);
    }
}