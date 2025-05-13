package com.crunchybet.betapp.service;

import com.crunchybet.betapp.dto.BetResponseDTO;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.repository.BetRepository;
import com.crunchybet.betapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private BetRepository betRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }



    public void updatePassword(String username, String oldPassword, String newPassword) {
        User user = findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Verify old password
//        if (!verifyPassword(oldPassword, user.getPassword())) {
//            throw new IllegalArgumentException("Current password is incorrect");
//        }

        // Check if new password is same as old password
        if (verifyPassword(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<BetResponseDTO> getBettingHistory(String username) {
        User user = findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        return betRepository.findByUserOrderByPlacedAtDesc(user).stream()
                .map(bet -> {
                    BetResponseDTO dto = new BetResponseDTO();
                    dto.setCategory(bet.getNominee().getCategory().getName());
                    dto.setNominee(bet.getNominee().getName());
                    dto.setAmount(bet.getAmount());
                    dto.setPlacedAt(bet.getPlacedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
