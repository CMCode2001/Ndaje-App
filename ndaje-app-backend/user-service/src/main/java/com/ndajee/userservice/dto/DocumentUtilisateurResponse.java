package com.ndajee.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUtilisateurResponse {
    private Long id;
    private String userId;
    private String nom;
    private String contentType;
    private Long taille;
    private String s3Key;
    private String typeDocument;
    private String numero;
    private LocalDate expiration;
    private String statut;
    private LocalDateTime dateUpload;
}
