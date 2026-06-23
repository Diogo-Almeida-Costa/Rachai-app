package com.rachai.framework.core.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;

    public static User fromModel(com.rachai.api.model.User model) {
        if (model == null) return null;
        return new User(model.getId(), model.getName(), model.getEmail());
    }
}
