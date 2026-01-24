package com.ndajee.documentservice.dto;

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
    private String type; // MIME type
    private Long taille;
    private LocalDateTime dateUpload;

    private String entityId;
    private String statut;
    private String typeDocument; // Enum string
    private String numero;
    private String urlS3;

    // Custom constructor if needed, or rely on AllArgsConstructor/Builder
}
