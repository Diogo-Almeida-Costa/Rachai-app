package com.rachai.api.model;

import javax.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "friendships")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id")
    private User friend;

    public Friendship(Long id, User user, User friend){
        this.id = id;
        this.user = user;
        this.friend = friend;
    }

}
