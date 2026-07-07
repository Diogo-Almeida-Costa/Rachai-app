package com.rachai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rachai.model.User;
import com.rachai.model.Friendship;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByUser(User user);

    boolean existsByUserAndFriend(User user, User friend);

    public void deleteByUser_IdAndFriend_Id(Long userId, Long friendId);
}
