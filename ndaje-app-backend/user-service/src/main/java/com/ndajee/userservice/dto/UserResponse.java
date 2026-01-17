package com.ndajee.userservice.dto;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private String role; // "PASSAGER" ou "CONDUCTEUR"
}
