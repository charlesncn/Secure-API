package com.example.demo_cyber_shujaa.controller;


import com.example.demo_cyber_shujaa.dto.APIResponse;
import com.example.demo_cyber_shujaa.dto.Login;
import com.example.demo_cyber_shujaa.dto.Register;
import com.example.demo_cyber_shujaa.dto.Response;
import com.example.demo_cyber_shujaa.service.AuthService;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<APIResponse> register(@RequestBody Register request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse> login(@RequestBody Login request) {
        return authService.authenticate(request);
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, java.io.IOException {
        authService.refreshToken(request, response);
    }

    @GetMapping("user/{name}")
    public ResponseEntity<Response> getUserByname(@PathVariable String name){
        return authService.getUserByName(name);
    }
}
