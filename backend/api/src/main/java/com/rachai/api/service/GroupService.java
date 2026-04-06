package com.rachai.api.service;

import com.rachai.api.model.Group;
import com.rachai.api.model.User;

import java.util.List;
import java.util.Optional;

public interface GroupService {
    Group createGroup(Group group, User owner);

    // Busca grupo por ID
    Optional<Group> getGroupById(Long id);

    List<Group> getAllGroups();
    Group updateGroup(Long id, Group groupDetails);
    void deleteGroup(Long id);
    Group addMemberToGroup(Long groupId, Long memberId);
    Group removeMemberFromGroup(Long groupId, Long memberId);
    List<Group> getGroupsByOwner(User owner);
    List<Group> getGroupsByMember(User member);
}
