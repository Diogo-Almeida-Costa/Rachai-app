package com.rachai.api.service;

import com.rachai.api.dto.FriendshipDTO;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.model.Friendship;
import com.rachai.api.model.User;
import com.rachai.api.service.UserService;
import com.rachai.api.repository.FriendshipRepository;
import com.rachai.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendshipService {
    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public FriendshipDTO addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Você não pode adicionar a si mesmo.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Amigo não encontrado"));

        // Evita duplicados
        if (friendshipRepository.existsByUserAndFriend(user, friend)) {
            throw new IllegalStateException("Vocês já são amigos.");
        }

        Friendship friendship = new Friendship(null, user, friend);
        Friendship saved = friendshipRepository.save(friendship);

        return convertToDTO(saved);
    }

    public List<FriendshipDTO> listFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        return friendshipRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        // Lógica para deletar a amizade
        // friendshipRepository.deleteByUserAndFriendId(userId, friendId);
    }

    private FriendshipDTO convertToDTO(Friendship friendship) {
        return new FriendshipDTO(
                friendship.getId(),
                friendship.getUser().getId(),
                friendship.getFriend().getId(),
                friendship.getFriend().getName(),
                friendship.getFriend().getEmail(),
                friendship.getFriend().getImageUrl()
        );
    }
}