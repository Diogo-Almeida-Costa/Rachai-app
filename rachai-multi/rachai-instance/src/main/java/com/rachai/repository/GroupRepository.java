package com.rachai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rachai.model.Group;
import com.rachai.model.User;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByOwner(User owner);
    List<Group> findByMembers(User member);
    List<Group> findByMembersContaining(User member);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.owner LEFT JOIN FETCH g.members")
    List<Group> findAllWithDetails();

}
