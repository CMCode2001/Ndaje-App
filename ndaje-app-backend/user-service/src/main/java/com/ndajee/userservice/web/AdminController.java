package com.ndajee.userservice.web;

import com.ndajee.userservice.dto.UserResponse;
import com.ndajee.userservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 1. List all users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // 2. Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    // 3. Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // 4. Update status (enable/disable)
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable String id, @RequestParam boolean active) {
        adminService.updateUserStatus(id, active);
        return ResponseEntity.noContent().build();
    }
}
