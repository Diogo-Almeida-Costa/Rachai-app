package com.rachai.api.dto;

import lombok.Setter;
import lombok.Getter;

@Getter @Setter
public class UserUpdateDTO {
    private String name;
    private String imageUrl;
    private String bio;
}
