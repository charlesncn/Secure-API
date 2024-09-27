package com.example.demo_cyber_shujaa.service;

import com.example.demo_cyber_shujaa.config.JwtService;
import com.example.demo_cyber_shujaa.dto.*;
import com.example.demo_cyber_shujaa.entity.Role;
import com.example.demo_cyber_shujaa.entity.SystemUser;
import com.example.demo_cyber_shujaa.entity.Token;
import com.example.demo_cyber_shujaa.exceptions.CustomException;
import com.example.demo_cyber_shujaa.exceptions.ErrorMsg;
import com.example.demo_cyber_shujaa.repository.TokenRepository;
import com.example.demo_cyber_shujaa.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, ObjectMapper objectMapper, JwtService jwtService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
    }

    public ResponseEntity<APIResponse> register(Register request) {
//        check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(APIResponse.errorResponse(1000, "Username already exists"));
        }
//        save user
        SystemUser user = new SystemUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.SYSTEM_USER);
        user.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        userRepository.save(user);

        var token = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(user, token);
        return ResponseEntity.ok().body(APIResponse.successResponse(token, refreshToken));

    }

    public ResponseEntity<APIResponse> authenticate(Login request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword())
        );
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(
                () -> new RuntimeException("User not found"));

        var token = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, token);

        return ResponseEntity.ok().body(APIResponse.successResponse(token, refreshToken));
    }

    public void saveUserToken(SystemUser user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .accessToken(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public void revokeAllUserTokens(SystemUser user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId()).orElseThrow(
                () -> new RuntimeException("No valid token found"));
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, java.io.IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userName;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        try {
            userName = jwtService.extractUsername(refreshToken);
        } catch (CustomException e) {
            return;
        }
        if (userName == null) {
            return;
        }
        var user = this.userRepository.findByUsername(userName)
                .orElseThrow();
        if (jwtService.isTokenValid(refreshToken, user)) {
            var accessToken = jwtService.generateToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, accessToken);
            var authResponse = APIResponse.successResponse(accessToken, refreshToken);
            objectMapper.writeValue(response.getOutputStream(), authResponse);
        }
    }

    public ResponseEntity<Response> getUserByName(String name) {
        var user = userRepository.findByUsername(name);

        return ResponseEntity.ofNullable(Response.ofSuccess( 0, user.isPresent() ? "Username : "+user.get().getUsername() : "User not found"));
    }
}

