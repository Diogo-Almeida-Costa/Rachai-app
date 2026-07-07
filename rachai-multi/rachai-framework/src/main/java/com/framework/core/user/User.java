package com.framework.core.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.framework.extension.user.IUser;

@Data
@NoArgsConstructor

public class User implements IUser{
    private Long id;
    private String name;
    private String email;

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static User fromModel(IUser model) {
        if (model == null) return null;
        return new User(model.getId(), model.getName(), model.getEmail());
    }

    @Override
    public Long getId() {
       return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEmail() {
        return email;
    }

}
