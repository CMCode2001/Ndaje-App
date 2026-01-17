package com.ndajee.userservice.web;

import com.ndajee.userservice.dto.UserRegistrationRequest;
import com.ndajee.userservice.dto.UserResponse;
import com.ndajee.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.ndajee.userservice.dto.UpdateProfileRequest;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register/passenger")
    public ResponseEntity<UserResponse> registerPassenger(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerPassager(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/register/driver")
    public ResponseEntity<UserResponse> registerDriver(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerConducteur(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    
    @PostMapping("/login")
    public ResponseEntity<com.ndajee.userservice.dto.TokenResponse> login(@Valid @RequestBody com.ndajee.userservice.dto.LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/profile")
    public ResponseEntity<UserResponse> updateProfile(@org.springframework.web.bind.annotation.PathVariable String id, @Valid @RequestBody com.ndajee.userservice.dto.UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(id, request);
        return ResponseEntity.ok(response);
    }
}
