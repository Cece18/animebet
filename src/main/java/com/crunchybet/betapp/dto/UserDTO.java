package com.crunchybet.betapp.dto;

import com.crunchybet.betapp.model.User;
import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String email;

    //Constructor to convert from User entity
    public UserDTO(User user) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
