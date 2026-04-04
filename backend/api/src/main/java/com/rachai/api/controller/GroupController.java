package com.rachai.api.controller;

import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.service.GroupService;
import com.rachai.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody Group group) {
        User owner = getAuthenticatedUser();
        Group createdGroup = groupService.createGroup(group, owner);
        return new ResponseEntity<>(createdGroup, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        return new ResponseEntity<>(groups, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        Optional<Group> group = groupService.getGroupById(id);
        return group.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable Long id, @RequestBody Group groupDetails) {
        try {
            Group updatedGroup = groupService.updateGroup(id, groupDetails);
            return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteGroup(@PathVariable Long id) {
        try {
            groupService.deleteGroup(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Group> addMemberToGroup(@PathVariable Long groupId, @PathVariable Long memberId) {
        try {
            User member = userRepository.findById(memberId).orElseThrow(() -> new RuntimeException("Member not found"));
            Group updatedGroup = groupService.addMemberToGroup(groupId, member);
            return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Group> removeMemberFromGroup(@PathVariable Long groupId, @PathVariable Long memberId) {
        try {
            User member = userRepository.findById(memberId).orElseThrow(() -> new RuntimeException("Member not found"));
            Group updatedGroup = groupService.removeMemberFromGroup(groupId, member);
            return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/owner")
    public ResponseEntity<List<Group>> getGroupsByOwner() {
        User owner = getAuthenticatedUser();
        List<Group> groups = groupService.getGroupsByOwner(owner);
        return new ResponseEntity<>(groups, HttpStatus.OK);
    }

    @GetMapping("/member")
    public ResponseEntity<List<Group>> getGroupsByMember() {
        User member = getAuthenticatedUser();
        List<Group> groups = groupService.getGroupsByMember(member);
        return new ResponseEntity<>(groups, HttpStatus.OK);
    }
}
