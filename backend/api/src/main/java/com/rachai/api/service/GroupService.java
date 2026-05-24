package com.rachai.api.service;

import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.repository.GroupRepository;
import com.rachai.api.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rachai.api.exception.BusinessException;


@Service
public class GroupService{

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

    public Group createGroup(Group group, Long ownerId) {
        logger.info("Creating Group!");

        User owner = userRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        group.setOwner(owner);
        group.getMembers().add(owner);
        return groupRepository.save(group);
    }

    public Group getGroupById(Long id) {
        logger.info("Searching Group by Id");

        return groupRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No Groups were found for this ID"));
    }

    public List<Group> getAllGroups() {
        logger.info("Finding All Groups!");

        return groupRepository.findAll();
    }

    public Group updateGroup(Long id, Group group) {
        logger.info("Updating Group with ID: {}", id);

        Group entity = groupRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No Groups were found for this ID"));
        entity.setName(group.getName());
        entity.setDescription(group.getDescription());

        return groupRepository.save(entity);
    }

    public void deleteGroup(Long id) {
        logger.info("Removing Group with ID: {}" , id);

        Group entity = groupRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No Groups were found for this ID"));

        groupRepository.delete(entity);
    }

    public Group addMemberToGroup(Long groupId, Long memberId) {
        logger.info("Adding member with Id: {} to Group {}" , memberId , groupId);

        Group entityGroup = groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("No Groups were found for this ID"));

        User entityMember = userRepository.findById(memberId).orElseThrow(() -> new ResourceNotFoundException("No Members were found for this ID"));

        if(entityGroup.getMembers().contains(entityMember)) {
            throw new BusinessException("User is already a member of this group");
        }
        
        entityGroup.getMembers().add(entityMember);

        return groupRepository.save(entityGroup);
    }

    public Group removeMemberFromGroup(Long groupId, Long memberId) {
        logger.info("Removing member with Id: {} to Group {}" , memberId , groupId);

        Group entityGroup = groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("No Groups were found for this ID"));

        User entityMember = userRepository.findById(memberId).orElseThrow(() -> new ResourceNotFoundException("No Members were found for this ID"));
        
        if(entityGroup.getOwner().getId().equals(memberId)) {
            throw new BusinessException("Owner cannot be removed from the group");
        }

        if(!entityGroup.getMembers().contains(entityMember)) {
            throw new BusinessException("User is not a member of this group");
        }

        entityGroup.getMembers().remove(entityMember);

        return groupRepository.save(entityGroup);
    }

    public List<Group> getGroupsByOwner(Long ownerId) {
        logger.info("Searching Groups by owner ID {}" , ownerId);

        User owner = userRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        return groupRepository.findByOwner(owner);
    }

    public List<Group> getGroupsByMember(Long memberId) {
        logger.info("Searching Groups by member {}" , memberId);

        User member = userRepository.findById(memberId).orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        return groupRepository.findByMembers(member);
    }
}
