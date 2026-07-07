package com.rachai.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rachai.dto.groupDTOs.GroupRequestDTO;
import com.rachai.dto.groupDTOs.GroupResponseDTO;
import com.framework.core.exception.BusinessException;
import com.framework.core.exception.ResourceNotFoundException;
import com.rachai.mapper.DozerMapper;
import com.rachai.model.Group;
import com.rachai.model.User;
import com.rachai.repository.GroupRepository;
import com.rachai.repository.UserRepository;


@Service
public class GroupService{

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

    //OK
    @Transactional
    public GroupResponseDTO createGroup(GroupRequestDTO dto, Long ownerId) {
        logger.info("Attempting to create group with name: '{}' for owner ID: {}", dto.getName(), ownerId);

        User owner = userRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        Group group = DozerMapper.parseObject(dto, Group.class);
        
        group.setOwner(owner);
        group.addMember(owner);

        Group savedGroup = groupRepository.save(group);

        return DozerMapper.parseObject(savedGroup, GroupResponseDTO.class);
    }

    //OK
    @Transactional(readOnly = true)
    public GroupResponseDTO getGroupById(Long id) {
        logger.info("Attempting to find group by ID: {}", id);

        Group group = findGroupOrThrow(id);

        return DozerMapper.parseObject(group, GroupResponseDTO.class);
    }

    //OK
    public List<GroupResponseDTO> getAllGroups() {
        logger.info("Attempting to list all groups with details");
        return groupRepository.findAllWithDetails().stream().map(group -> DozerMapper.parseObject(group, GroupResponseDTO.class)).toList();
    }

    //OK
    @Transactional
    public GroupResponseDTO updateGroup(Long id, GroupRequestDTO dto, Long authenticatedUserId) {
        logger.info("User ID: {} is attempting to update group with ID: {}", authenticatedUserId, id);

        Group entity = findGroupOrThrow(id);

        if (!entity.getOwner().getId().equals(authenticatedUserId)) {
            logger.warn("Security alert: User ID: {} tried to update group ID: {} without ownership permission!", authenticatedUserId, id);
            throw new IllegalStateException("Você não tem permissão para alterar este grupo, pois você não é o proprietário.");
        }

        DozerMapper.mergeObject(dto, entity);

        Group updatedGroup = groupRepository.save(entity);

        return DozerMapper.parseObject(updatedGroup, GroupResponseDTO.class);
    }

    //OK
    @Transactional
    public void deleteGroup(Long id, Long authenticatedUserId) {
        logger.info("User ID: {} is attempting to delete group with ID: {}", authenticatedUserId, id);

        Group entity = findGroupOrThrow(id);

        if (!entity.getOwner().getId().equals(authenticatedUserId)) {
            logger.warn("Security alert: User ID: {} tried to delete group ID: {} without ownership permission!", authenticatedUserId, id);
            throw new IllegalStateException("Você não tem permissão para deletar este grupo, pois você não é o proprietário.");
        }

        groupRepository.delete(entity);
    }

    //OK
    @Transactional
    public GroupResponseDTO addMemberToGroup(Long groupId, Long memberId, Long authenticatedUserId) {
        logger.info("User ID: {} is attempting to add member ID: {} to group ID: {}", authenticatedUserId, memberId, groupId);

        Group group = findGroupOrThrow(groupId);
        
        User newMember = userRepository.findById(memberId).orElseThrow(() -> new ResourceNotFoundException("No users were found for this ID"));

        if (!group.getOwner().getId().equals(authenticatedUserId)) {
            logger.warn("Security alert: User ID: {} tried to add a member to group ID: {} without permission!", authenticatedUserId, groupId);
            throw new IllegalStateException("Você não tem permissão para adicionar membros a este grupo, pois você não é o proprietário.");
        }

        if (group.getMembers().contains(newMember)) {
            throw new BusinessException("This user is already a member of this group");
        }

        group.addMember(newMember);

        Group updatedGroup = groupRepository.save(group);

        return DozerMapper.parseObject(updatedGroup, GroupResponseDTO.class);
    }

    //OK
    @Transactional
    public GroupResponseDTO removeMemberFromGroup(Long groupId, Long memberId, Long authenticatedUserId) {
        logger.info("User ID: {} is attempting to remove member ID: {} from group ID: {}", authenticatedUserId, memberId, groupId);

        Group group = findGroupOrThrow(groupId);
        
        User memberToRemove = userRepository.findById(memberId).orElseThrow(() -> new ResourceNotFoundException("No users were found for this ID"));

        boolean isOwner = group.getOwner().getId().equals(authenticatedUserId);
        boolean isSelfRemoving = memberId.equals(authenticatedUserId);

        if (!isOwner && !isSelfRemoving) {
            logger.warn("Security alert: User ID: {} tried to remove member ID: {} from group ID: {} without permission!", authenticatedUserId, memberId, groupId);
            throw new IllegalStateException("Você não tem permissão para remover este membro ou sair deste grupo.");
        }

        if (group.getOwner().getId().equals(memberId)) {
            throw new BusinessException("Owner cannot be removed from the group");
        }

        if (!group.getMembers().contains(memberToRemove)) {
            throw new BusinessException("User is not a member of this group");
        }

        group.removeMember(memberToRemove);

        Group updatedGroup = groupRepository.save(group);
        return DozerMapper.parseObject(updatedGroup, GroupResponseDTO.class);
    }

    //OK
    @Transactional(readOnly = true)
    public List<GroupResponseDTO> getGroupsByOwner(Long ownerId) {
        logger.info("Attempting to find groups by owner ID: {}", ownerId);

        User owner = userRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        return groupRepository.findByOwner(owner).stream().map(group -> DozerMapper.parseObject(group, GroupResponseDTO.class)).toList();
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDTO> getGroupsByMember(Long memberId) {
        logger.info("Attempting to find groups where user ID: {} is a member", memberId);

        User member = userRepository.findById(memberId).orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        return groupRepository.findByMembersContaining(member).stream().map(group -> DozerMapper.parseObject(group, GroupResponseDTO.class)).toList();
    }

    //Método auxiliar
    private Group findGroupOrThrow(Long id) {
        return groupRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No groups were found for this ID"));
    }
}
