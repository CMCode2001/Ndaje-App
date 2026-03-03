package com.ndajee.carservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Document associé à un véhicule (carte grise, assurance, contrôle
 * technique...).
 * Géré directement par car-service — stockage physique dans MinIO.
 */
@Entity
@Table(name = "vehicule_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID du véhicule propriétaire */
    @Column(nullable = false)
    private Long vehiculeId;

    /** Nom original du fichier */
    @Column(nullable = false)
    private String nom;

    /** Type MIME (application/pdf, image/jpeg...) */
    private String type;

    /** Taille en octets */
    private Long taille;

    /** Clé S3/MinIO pour accéder au fichier physique */
    @Column(nullable = false, unique = true)
    private String s3Key;

    /** Type de document (CARTE_GRISE, ASSURANCE, CONTROLE_TECHNIQUE...) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDocumentVehicule typeDocument;

    /** Numéro du document (optionnel) */
    private String numero;

    /** Date d'expiration (optionnel) */
    private LocalDate expiration;

    /** Statut de validation du document */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDocument statut;

    /** Date d'upload */
    @Column(nullable = false)
    private LocalDateTime dateUpload;

    @PrePersist
    protected void onCreate() {
        this.dateUpload = LocalDateTime.now();
        if (this.statut == null)
            this.statut = StatutDocument.SOUMIS;
    }
}
