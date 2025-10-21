package com.angelolfreitas.login_auth_api.controller;

import com.angelolfreitas.login_auth_api.domain.user.User;
import com.angelolfreitas.login_auth_api.dto.LoginRequest;
import com.angelolfreitas.login_auth_api.dto.LoginResponse;
import com.angelolfreitas.login_auth_api.dto.RegisterRequest;
import com.angelolfreitas.login_auth_api.dto.RegisterResponse;
import com.angelolfreitas.login_auth_api.infra.security.TokenService;
import com.angelolfreitas.login_auth_api.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        User user = this.userRepository.findByEmail(loginRequest.email())
                .orElseThrow(()->new RuntimeException("user not found"));
        if(!passwordEncoder.matches(loginRequest.password(), user.getPassword()))
            return ResponseEntity.badRequest().build();

        String token = this.tokenService.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(loginRequest.email(),token));
    }
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest){
        Optional<User> verifyUser = this.userRepository.findByEmail(registerRequest.email());
        if(verifyUser.isPresent())
            return ResponseEntity.badRequest().build();

        User user = User.builder()
                .password(passwordEncoder.encode(registerRequest.password()))
                .email(registerRequest.email())
                .name(registerRequest.name())
                .build();
        
        this.userRepository.save(user);

        String token = this.tokenService.generateToken(user);

        return ResponseEntity.ok(new RegisterResponse(user.getName(),token));
    }
}
