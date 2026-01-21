package com.ndajee.documentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String urlS3; // S3 object key

    @Column(nullable = false)
    private String type; // MIME type

    @Column(nullable = false)
    private Long taille; // File size in bytes

    @Column(nullable = false)
    private LocalDateTime dateUpload;

    @Column(nullable = false)
    private String utilisateurId;

    @PrePersist
    protected void onCreate() {
        dateUpload = LocalDateTime.now();
    }
}
