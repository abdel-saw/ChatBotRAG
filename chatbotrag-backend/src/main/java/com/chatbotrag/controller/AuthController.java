package com.chatbotrag.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.chatbotrag.config.JwtUtil;
import com.chatbotrag.dto.auth.AuthResponse;
import com.chatbotrag.dto.auth.LoginRequest;
import com.chatbotrag.dto.auth.RegisterRequest;
import com.chatbotrag.entity.User;
import com.chatbotrag.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return ResponseEntity.ok(new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userService.findByEmail(request.getEmail()); // tu devras ajouter cette méthode

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return ResponseEntity.ok(new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getName()));
    }
}