package com.rachai.dto.groupDTOs;

import com.rachai.dto.userDTOs.UserProfileDTO;

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
