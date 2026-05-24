package com.rachai.api.dto;

import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
public class UserProfileDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String imageUrl;
    private String bio;

    public UserProfileDTO() {
    }

    public UserProfileDTO(String firstName, String lastName, String email, String imageUrl, String bio) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.imageUrl = imageUrl;
        this.bio = bio;
    }
}