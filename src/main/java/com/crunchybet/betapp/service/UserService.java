package com.crunchybet.betapp.service;

import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
private UserRepository userRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public void updateUserPassword(User user, String newPassword) {
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    public void registerUser(User user) {
        userRepository.save(user);
    }

    public boolean verifyPassword(String password, String password1) {
        return password.equals(password1);
    }
}
