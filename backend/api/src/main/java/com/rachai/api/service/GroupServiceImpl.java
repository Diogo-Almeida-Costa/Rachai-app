package com.rachai.api.service;

import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.repository.GroupRepository;
import com.rachai.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    // Cria um novo grupo
    @Override
    public Group createGroup(Group group, User owner) {
        group.setOwner(owner);
        group.getMembers().add(owner);
        return groupRepository.save(group);
    }

    // Busca grupo por id
    @Override
    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    // Lista grupos existentes
    @Override
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    // Atualiza grupo
    @Override
    public Group updateGroup(Long id, Group groupDetails) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));
        group.setName(groupDetails.getName());
        group.setDescription(groupDetails.getDescription());
        return groupRepository.save(group);
    }

    // Deleta grupo
    @Override
    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    // Adiciona membro ao grupo
    @Override
    public Group addMemberToGroup(Long groupId, User member) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        User existingMember = userRepository.findById(member.getId()).orElseThrow(() -> new RuntimeException("Member not found"));
        group.getMembers().add(existingMember);
        return groupRepository.save(group);
    }

    // Remove membro de um grupo
    @Override
    public Group removeMemberFromGroup(Long groupId, User member) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        User existingMember = userRepository.findById(member.getId()).orElseThrow(() -> new RuntimeException("Member not found"));
        group.getMembers().remove(existingMember);
        return groupRepository.save(group);
    }

    // Lista grupos por dono
    @Override
    public List<Group> getGroupsByOwner(User owner) {
        return groupRepository.findByOwner(owner);
    }

    // Lista grupos por membro
    @Override
    public List<Group> getGroupsByMember(User member) {
        return groupRepository.findByMembersContaining(member);
    }
}
