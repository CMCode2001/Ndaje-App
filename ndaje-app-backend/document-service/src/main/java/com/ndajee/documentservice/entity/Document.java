package com.ndajee.documentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un document stocké dans le système.
 * Contient les métadonnées du fichier et sa référence vers le stockage Cloudflare R2/S3.
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

    /** Clé unique de l'objet dans le stockage Cloudflare R2 (ex: userId/uuid-filename) */
    @Column(nullable = false, unique = true)
    private String urlS3;

    /** Type MIME du fichier (ex: application/pdf, image/jpeg) */
    @Column(nullable = false)
    private String type;

    /** Taille du fichier en octets */
    @Column(nullable = false)
    private Long taille;

    /** Date et heure de l'upload du document */
    @Column(nullable = false)
    private LocalDateTime dateUpload;

    /** Identifiant de l'utilisateur (UUID Keycloak) propriétaire du document */
    @Column(nullable = false)
    private String utilisateurId;

    @PrePersist
    protected void onCreate() {
        dateUpload = LocalDateTime.now();
    }
}
