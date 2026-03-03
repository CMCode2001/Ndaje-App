package com.ndajee.userservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Document d'identité ou justificatif associé à un utilisateur (conducteur,
 * caravannier...).
 * Stocké physiquement dans MinIO sous le préfixe 'users/{userId}/'.
 */
@Entity
@Table(name = "utilisateur_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Keycloak ID de l'utilisateur propriétaire */
    @Column(nullable = false)
    private String userId;

    /** Nom original du fichier */
    @Column(nullable = false)
    private String nom;

    /** Type MIME (application/pdf, image/jpeg...) */
    private String contentType;

    /** Taille en octets */
    private Long taille;

    /** Clé S3/MinIO (chemin dans le bucket) */
    @Column(nullable = false, unique = true)
    private String s3Key;

    /** Type fonctionnel du document */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDocumentUtilisateur typeDocument;

    /** Numéro du document (optionnel, ex: numéro de permis) */
    private String numero;

    /** Date d'expiration du document (optionnel) */
    private LocalDate expiration;

    /** Statut de validation par un admin */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDocument statut;

    /** Date d'upload */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateUpload;

    @PrePersist
    protected void onCreate() {
        this.dateUpload = LocalDateTime.now();
        if (this.statut == null)
            this.statut = StatutDocument.SOUMIS;
    }
}
