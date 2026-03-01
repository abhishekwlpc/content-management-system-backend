package org.example.content_upload_system.service;

import lombok.RequiredArgsConstructor;

import org.example.content_upload_system.dto.LoginRequest;
import org.example.content_upload_system.dto.RegisterRequest;
import org.example.content_upload_system.entity.Instructor;
import org.example.content_upload_system.repository.InstructorRepository;
import org.example.content_upload_system.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final InstructorRepository instructorRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public Instructor register(RegisterRequest request) {

        if (instructorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Instructor instructor = new Instructor();

        instructor.setName(request.getName());
        instructor.setEmail(request.getEmail());
        instructor.setPassword(passwordEncoder.encode(request.getPassword()));

        return instructorRepository.save(instructor);
    }

    public String login(LoginRequest request) {

        Instructor instructor = instructorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!passwordEncoder.matches(request.getPassword(), instructor.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(instructor.getEmail());
        return token;
    }
}