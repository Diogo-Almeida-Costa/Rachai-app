package com.rachai.api.dto.friendshipDTOs;

import com.rachai.api.dto.userDTOs.UserProfileDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipDTO {
    private Long id;
    private UserProfileDTO user;
    private UserProfileDTO friend;
}
