
package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.RegisterRequest;
import com.decisionhub.backend.dto.LoginRequest;
import com.decisionhub.backend.dto.AuthResponse;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.AccountStatus;
import com.decisionhub.backend.exception.BadRequestException;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.repository.UserRepository;
import com.decisionhub.backend.security.JwtService;
import com.decisionhub.backend.service.AuthService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public Long registerUser(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    @Override
    public AuthResponse loginUser(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new ForbiddenException("Account is not active");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
