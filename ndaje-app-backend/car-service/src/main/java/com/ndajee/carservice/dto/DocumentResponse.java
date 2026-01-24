package com.ndajee.carservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String nom;
    private String type;
    private Long taille;
    private LocalDateTime dateUpload;
    private String entityId;
    private String statut;
    private String typeDocument;
    private String numero;
    private String urlS3;
    private java.time.LocalDate expiration;
}
