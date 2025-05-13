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
    public UserDTO() {
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
