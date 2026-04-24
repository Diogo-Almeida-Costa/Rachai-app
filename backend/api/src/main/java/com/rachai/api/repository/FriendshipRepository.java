package com.rachai.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rachai.api.model.User;
import com.rachai.api.model.Friendship;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByUserId(User user);

    boolean existsByUserAndFriend(User user, User friend);
}
