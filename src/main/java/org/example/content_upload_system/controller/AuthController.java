package org.example.content_upload_system.controller;

import lombok.RequiredArgsConstructor;
import org.example.content_upload_system.dto.LoginRequest;
import org.example.content_upload_system.dto.RegisterRequest;
import org.example.content_upload_system.entity.Instructor;
import org.example.content_upload_system.service.AuthService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Instructor register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }


    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}