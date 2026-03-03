package com.ndajee.carservice.dto;

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
public class DocumentResponse {
    private Long id;
    private Long vehiculeId;
    private String nom;
    private String type; // MIME type
    private Long taille;
    private String s3Key;
    private String typeDocument; // TypeDocumentVehicule
    private String numero;
    private LocalDate expiration;
    private String statut; // StatutDocument
    private LocalDateTime dateUpload;
}
