package com.rachai.api.controller;

import com.rachai.api.dto.GroupSuggestionDTO;
import com.rachai.api.exception.ResourceNotFoundException;
import com.rachai.api.model.Group;
import com.rachai.api.model.User;
import com.rachai.api.service.AiGroupSuggestionService;
import com.rachai.api.service.GroupService;
import com.rachai.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

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
        Group group = groupService.getGroupById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));
        return new ResponseEntity<>(group, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable Long id, @RequestBody Group groupDetails) {
        Group updatedGroup = groupService.updateGroup(id, groupDetails);
        return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Group> addMemberToGroup(@PathVariable Long groupId, @PathVariable Long memberId) {
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado"));
        Group updatedGroup = groupService.addMemberToGroup(groupId, member);
        return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Group> removeMemberFromGroup(@PathVariable Long groupId, @PathVariable Long memberId) {
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado"));
        Group updatedGroup = groupService.removeMemberFromGroup(groupId, member);
        return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
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

    /**
     * POST /api/groups/suggest
     * Gera uma sugestão de grupo com IA baseada no histórico do usuário.
     * Body (opcional): { "context": "viagem para a praia com os amigos" }
     */
    @PostMapping("/suggest")
    public ResponseEntity<GroupSuggestionDTO> suggestGroup(@RequestBody(required = false) Map<String, String> body) {
        User currentUser = getAuthenticatedUser();
        List<Group> userGroups = groupService.getGroupsByMember(currentUser);
        String context = body != null ? body.get("context") : null;

        GroupSuggestionDTO suggestion = aiGroupSuggestionService.suggestGroup(currentUser, userGroups, context);
        return ResponseEntity.ok(suggestion);
    }

    /**
     * POST /api/groups/suggest/confirm
     * Cria o grupo confirmado pelo usuário a partir de uma sugestão da IA.
     * Body: { "name": "...", "description": "...", "memberIds": [1, 2, 3] }
     */
    @PostMapping("/suggest/confirm")
    public ResponseEntity<Group> confirmSuggestedGroup(@RequestBody Map<String, Object> body) {
        User owner = getAuthenticatedUser();

        String name = body.containsKey("name")
                ? (String) body.get("name")
                : (String) body.get("suggestedName");

        String description = body.containsKey("description")
                ? (String) body.get("description")
                : (String) body.get("suggestedDescription");

        @SuppressWarnings("unchecked")
        List<Integer> memberIds = body.containsKey("memberIds")
                ? (List<Integer>) body.get("memberIds")
                : (List<Integer>) body.get("suggestedMemberIds");

        Group group = new Group();
        group.setName(name);
        group.setDescription(description);

        Group createdGroup = groupService.createGroup(group, owner);

        if (memberIds != null) {
            for (Integer memberId : memberIds) {
                userRepository.findById(Long.valueOf(memberId))
                        .ifPresent(member -> groupService.addMemberToGroup(createdGroup.getId(), member));
            }
        }

        Group finalGroup = groupService.getGroupById(createdGroup.getId())
                .orElse(createdGroup);
        return new ResponseEntity<>(finalGroup, HttpStatus.CREATED);
    }
}