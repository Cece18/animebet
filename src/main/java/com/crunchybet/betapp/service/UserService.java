package com.crunchybet.betapp.service;

import com.crunchybet.betapp.dto.BetResponseDTO;
import com.crunchybet.betapp.dto.UserDTO;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.repository.BetRepository;
import com.crunchybet.betapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private BetRepository betRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))  // Add ROLE_ prefix
        );
    }

    @Cacheable(value = "usersByUsername", key = "#p0")
    public User findByUsername(String email) {
        return userRepository.findByEmail(email);
    }

    @Cacheable(value = "usersByEmail", key = "#email")
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    @Transactional
    public void createUser(UserDTO userDTO) {
        String email = userDTO.getEmail().toLowerCase();
        String username = userDTO.getUsername().toLowerCase();

        if (userRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setPoints(1000); // or whatever your default is

        userRepository.save(user);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }



    @Transactional
    public void updatePassword(String username, String oldPassword, String newPassword) {
        User user = findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        // Verify old password
        if (!verifyPassword(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

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
                    dto.setId(bet.getId());
                    dto.setCategory(bet.getNominee().getCategory().getName());
                    dto.setNominee(bet.getNominee().getName());
                    dto.setAmount(bet.getAmount());
                    dto.setPlacedAt(bet.getPlacedAt());
                    dto.setStatus(bet.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<BetResponseDTO> getBetsByCategory(String username, Long categoryId) {
        User user = findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        return betRepository.findByUserAndCategoryId(user, categoryId).stream()
                .map(bet -> {
                    BetResponseDTO dto = new BetResponseDTO();
                    dto.setId(bet.getId());
                    dto.setCategory(bet.getNominee().getCategory().getName());
                    dto.setNominee(bet.getNominee().getName());
                    dto.setAmount(bet.getAmount());
                    dto.setPlacedAt(bet.getPlacedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public void initiatePasswordReset(String email) {
        User user = findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // Generate 6-digit code
        String resetCode = String.format("%06d", new Random().nextInt(999999));
        user.setResetCode(resetCode);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Send email with code
        emailService.sendResetCode(email, resetCode);
    }

    @Transactional
    public boolean verifyResetCode(String email, String code) {
        User user = findByEmail(email);
        if (user == null || user.getResetCode() == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(user.getResetCodeExpiry())) {
            // Code has expired
            user.setResetCode(null);
            user.setResetCodeExpiry(null);
            userRepository.save(user);
            return false;
        }

        return user.getResetCode().equals(code);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        if (!verifyResetCode(email, code)) {
            throw new IllegalArgumentException("Invalid or expired reset code");
        }

        User user = findByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        userRepository.save(user);
    }

}
