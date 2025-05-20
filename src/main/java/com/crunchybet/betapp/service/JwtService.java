package com.crunchybet.betapp.service;

import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {
    private final UserRepository userRepository;
    private final String secretKey = "5D68E79BCED4A2442A72DD7F6A4A75D68E79BCED4A2442A72DD7F6A4A7";
    private final long expirationTime = 86400000; // 24 hours in milliseconds

    @Autowired
    public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateToken(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "ROLE_" + user.getRole());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public List<String> extractRoles(String token) {
        final Claims claims = extractAllClaims(token);
        String rolesStr = claims.get("roles", String.class);
        return rolesStr != null ? Collections.singletonList(rolesStr) : Collections.emptyList();
    }

    public boolean hasRole(String token, String requiredRole) {
        List<String> roles = extractRoles(token);
        return roles.contains(requiredRole);
    }
}