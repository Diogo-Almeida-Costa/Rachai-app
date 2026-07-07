package com.rachai.dto.userDTOs;

import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String imageUrl;
    private String bio; 
}