package com.rachai.api.dto.groupDTOs;

import com.rachai.api.dto.userDTOs.UserProfileDTO;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponseDTO {
    private Long id;
    private String name;
    private String description;
    private UserProfileDTO owner;
    private Set<UserProfileDTO> members;
}
