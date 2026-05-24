package com.rachai.api.controller;

import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.service.GroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.MediaType;

@RestController
@RequestMapping("/rachai/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    private User getAuthenticatedUser() {
       return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @RequestMapping(method = RequestMethod.POST , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> createGroup(@RequestBody Group group) {

        User owner = getAuthenticatedUser();

        Group createdGroup = groupService.createGroup(group, owner.getId());

        return new ResponseEntity<>(createdGroup, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Group>> getAllGroups() {

        List<Group> groups = groupService.getAllGroups();

        return new ResponseEntity<>(groups, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}" , method = RequestMethod.GET , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {

        Group group = groupService.getGroupById(id);
        
        return new ResponseEntity<>(group, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}" , method = RequestMethod.PUT , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> updateGroup(@PathVariable Long id, @RequestBody Group groupDetails) {

        Group updatedGroup = groupService.updateGroup(id, groupDetails);

        return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}" , method = RequestMethod.DELETE , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Group> addMemberToGroup(@PathVariable Long groupId, @PathVariable Long memberId) {
        Group updatedGroup = groupService.addMemberToGroup(groupId, memberId);

        return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Group> removeMemberFromGroup(@PathVariable Long groupId, @PathVariable Long memberId) {

        Group updatedGroup = groupService.removeMemberFromGroup(groupId, memberId);
        
        return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<Group>> getGroupsByOwner() {
        User owner = getAuthenticatedUser();

        List<Group> groups = groupService.getGroupsByOwner(owner.getId());

        return new ResponseEntity<>(groups, HttpStatus.OK);
    }

    @GetMapping("/member")
    public ResponseEntity<List<Group>> getGroupsByMember() {
        User member = getAuthenticatedUser();

        List<Group> groups = groupService.getGroupsByMember(member.getId());

        return new ResponseEntity<>(groups, HttpStatus.OK);
    }
}
