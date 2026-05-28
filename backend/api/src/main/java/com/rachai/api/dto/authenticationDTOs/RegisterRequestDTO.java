package com.rachai.api.dto.authenticationDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO{
    String email;
    String password;
    String firstName;
    String lastName;
    String imageUrl;
    String bio;
}