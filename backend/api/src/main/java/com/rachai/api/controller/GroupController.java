package com.rachai.api.controller;

import com.rachai.api.dto.GroupSuggestionDTO;
import com.rachai.api.dto.groupDTOs.GroupRequestDTO;
import com.rachai.api.dto.groupDTOs.GroupResponseDTO;
import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.service.AiGroupSuggestionService;
import com.rachai.api.service.GroupService;
import com.rachai.api.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rachai/groups")
public class GroupController {

    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiGroupSuggestionService aiGroupSuggestionService;

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping
    public ResponseEntity<GroupResponseDTO> createGroup(@RequestBody GroupRequestDTO dto) {
        User owner = getAuthenticatedUser();
        logger.info("HTTP POST request received to create group: {} by User ID: {}", dto.getName(), owner.getId());
        
        GroupResponseDTO createdGroup = groupService.createGroup(dto, owner.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponseDTO>> getAllGroups() {
        logger.info("HTTP GET request received to fetch all available groups");
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponseDTO> getGroupById(@PathVariable Long id) {
        logger.info("HTTP GET request received for group ID: {}", id);
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponseDTO> updateGroup(@PathVariable Long id, @RequestBody GroupRequestDTO dto) {
        User requester = getAuthenticatedUser();
        logger.info("HTTP PUT request received to update group ID: {} by User ID: {}", id, requester.getId());
        
        GroupResponseDTO updatedGroup = groupService.updateGroup(id, dto, requester.getId());
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        User requester = getAuthenticatedUser();
        logger.info("HTTP DELETE request received for group ID: {} by User ID: {}", id, requester.getId());
        
        groupService.deleteGroup(id, requester.getId());
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<GroupResponseDTO> addMemberToGroup(@PathVariable Long groupId, @PathVariable Long memberId) {
        User requester = getAuthenticatedUser();
        logger.info("User ID: {} is attempting to add Member ID: {} to group ID: {}", requester.getId(), memberId, groupId);
        
        GroupResponseDTO updatedGroup = groupService.addMemberToGroup(groupId, memberId, requester.getId());
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<GroupResponseDTO> removeMemberFromGroup(@PathVariable Long groupId, @PathVariable Long memberId) {
        User requester = getAuthenticatedUser();
        logger.info("User ID: {} is attempting to remove Member ID: {} from group ID: {}", requester.getId(), memberId, groupId);
        
        GroupResponseDTO updatedGroup = groupService.removeMemberFromGroup(groupId, memberId, requester.getId());
        return ResponseEntity.ok(updatedGroup);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<GroupResponseDTO>> getGroupsByOwner() {
        User owner = getAuthenticatedUser();
        logger.info("HTTP GET request received to list groups owned by user ID: {}", owner.getId());
        return ResponseEntity.ok(groupService.getGroupsByOwner(owner.getId()));
    }

    @GetMapping("/member")
    public ResponseEntity<List<GroupResponseDTO>> getGroupsByMember() {
        User member = getAuthenticatedUser();
        logger.info("HTTP GET request received to list groups where user ID: {} is a member", member.getId());
        return ResponseEntity.ok(groupService.getGroupsByMember(member.getId()));
    }

    @PostMapping("/suggest")
    public ResponseEntity<GroupSuggestionDTO> suggestGroup(@RequestBody(required = false) Map<String, String> body) {
        User currentUser = getAuthenticatedUser();
        String context = body != null ? body.get("context") : null;
        logger.info("HTTP POST request received for AI group suggestion with context: {}", context);

        List<GroupResponseDTO> userGroupsResponse = groupService.getGroupsByMember(currentUser.getId());
        
        List<Group> userGroups = userGroupsResponse.stream().map(dto -> {
            Group g = new Group();
            g.setId(dto.getId());
            g.setName(dto.getName());
            g.setDescription(dto.getDescription());
            return g;
        }).toList();

        GroupSuggestionDTO suggestion = aiGroupSuggestionService.suggestGroup(currentUser, userGroups, context);
        return ResponseEntity.ok(suggestion);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/suggest/confirm")
    public ResponseEntity<GroupResponseDTO> confirmSuggestedGroup(@RequestBody Map<String, Object> body) {
        User owner = getAuthenticatedUser();
        logger.info("HTTP POST request received to confirm AI suggested group for user ID: {}", owner.getId());

        String name = body.containsKey("name") ? (String) body.get("name") : (String) body.get("suggestedName");
        String description = body.containsKey("description") ? (String) body.get("description") : (String) body.get("suggestedDescription");

        List<Integer> memberIds = body.containsKey("memberIds") 
                ? (List<Integer>) body.get("memberIds") 
                : (List<Integer>) body.get("suggestedMemberIds");


        GroupRequestDTO requestDto = new GroupRequestDTO();
        requestDto.setName(name);
        requestDto.setDescription(description);

        GroupResponseDTO createdGroup = groupService.createGroup(requestDto, owner.getId());

        if (memberIds != null) {
            for (Integer memberId : memberIds) {
                userRepository.findById(Long.valueOf(memberId))
                        .ifPresent(member -> groupService.addMemberToGroup(createdGroup.getId(), member.getId(), owner.getId()));
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.getGroupById(createdGroup.getId()));
    }
}