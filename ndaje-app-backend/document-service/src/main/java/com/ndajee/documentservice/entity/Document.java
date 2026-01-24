package com.ndajee.documentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un document stocké dans le système.
 * Contient les métadonnées du fichier et sa référence vers le stockage
 * Cloudflare R2/S3.
 */
@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom d'origine du fichier lors de l'upload */
    @Column(nullable = false)
    private String nom;

    /**
     * Clé unique de l'objet dans le stockage Cloudflare R2 (ex:
     * userId/uuid-filename)
     */
    @Column(nullable = false, unique = true)
    private String urlS3;

    @Column(nullable = false)
    private String numero;

    private java.time.LocalDate expiration;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Long taille;

    @Column(nullable = false)
    private java.time.LocalDateTime dateUpload;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatutDocument statutDocument;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeDocument typeDocument; // Renamed from 'type' to avoid confusion or keep 'type' if preferred, but
                                       // diagram says 'type' references TypeDocument

    // Generic Association
    @Column(nullable = false)
    private String entityId; // ID of User or Vehicle

    @Column(nullable = false)
    private String entityType; // "USER" or "VEHICLE"

    @PrePersist
    protected void onCreate() {
        dateUpload = LocalDateTime.now();
    }
}
