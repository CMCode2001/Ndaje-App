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
    private String type;
    private Long taille;
    private LocalDateTime dateUpload;
    private String utilisateurId;
    private String urlS3;
}
