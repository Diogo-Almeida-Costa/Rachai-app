package com.rachai.api.dto;

import java.util.List;


import lombok.*;

public class FriendshipDTO {
    private Long id;
    private Long userId;
    private Long friendId;
    private String friendName;
    private String friendEmail;
    private String friendProfilePicture;

    public FriendshipDTO() {
    }

    public FriendshipDTO(Long id, Long userId, Long friendId, String friendName, 
                         String friendEmail, String friendProfilePicture) {
        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
        this.friendName = friendName;
        this.friendEmail = friendEmail;
        this.friendProfilePicture = friendProfilePicture;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getFriendId() { return friendId; }
    public String getFriendName() { return friendName; }
    public String getFriendEmail() { return friendEmail; }
    public String getFriendProfilePicture() { return friendProfilePicture; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setFriendId(Long friendId) { this.friendId = friendId; }
    public void setFriendName(String friendName) { this.friendName = friendName; }
    public void setFriendEmail(String friendEmail) { this.friendEmail = friendEmail; }
    public void setFriendProfilePicture(String friendProfilePicture) { this.friendProfilePicture = friendProfilePicture; }
}
