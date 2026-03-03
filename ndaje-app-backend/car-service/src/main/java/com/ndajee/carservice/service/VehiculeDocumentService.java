package com.ndajee.carservice.service;

import com.ndajee.carservice.domain.StatutDocument;
import com.ndajee.carservice.domain.TypeDocumentVehicule;
import com.ndajee.carservice.domain.VehiculeDocument;
import com.ndajee.carservice.dto.DocumentResponse;
import com.ndajee.carservice.exception.ResourceNotFoundException;
import com.ndajee.carservice.repository.VehiculeDocumentRepository;
import com.ndajee.carservice.storage.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service local pour la gestion des documents de véhicules.
 * Remplace l'appel Feign vers document-service : tout est géré en interne.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehiculeDocumentService {

    private final VehiculeDocumentRepository documentRepository;
    private final S3StorageService s3StorageService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Upload un document pour un véhicule et enregistre ses métadonnées.
     */
    @Transactional
    public DocumentResponse uploadDocument(Long vehiculeId, MultipartFile file,
            String typeDocument, String numero, String expirationStr) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("La taille du fichier dépasse 10MB");
        }

        // Upload physique vers MinIO
        String s3Key = s3StorageService.uploadFile(file, String.valueOf(vehiculeId));

        // Résoudre le type de document
        TypeDocumentVehicule type;
        try {
            type = TypeDocumentVehicule.valueOf(typeDocument.toUpperCase());
        } catch (Exception e) {
            log.warn("Type de document inconnu: {}, utilisation de AUTRE", typeDocument);
            type = TypeDocumentVehicule.AUTRE;
        }

        // Parser la date d'expiration
        LocalDate expiration = null;
        if (expirationStr != null && !expirationStr.isBlank()) {
            try {
                expiration = LocalDate.parse(expirationStr);
            } catch (Exception e) {
                log.warn("Date d'expiration invalide: {}", expirationStr);
            }
        }

        VehiculeDocument document = VehiculeDocument.builder()
                .vehiculeId(vehiculeId)
                .nom(file.getOriginalFilename())
                .type(file.getContentType())
                .taille(file.getSize())
                .s3Key(s3Key)
                .typeDocument(type)
                .numero(numero)
                .expiration(expiration)
                .statut(StatutDocument.SOUMIS)
                .build();

        VehiculeDocument saved = documentRepository.save(document);
        log.info("Document enregistré pour le véhicule {}: id={}", vehiculeId, saved.getId());
        return toResponse(saved);
    }

    /**
     * Retourne la liste des documents d'un véhicule.
     */
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByVehicule(Long vehiculeId) {
        return documentRepository.findByVehiculeId(vehiculeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Télécharge le contenu binaire d'un document.
     */
    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long documentId) {
        VehiculeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
        return s3StorageService.downloadFile(document.getS3Key());
    }

    /**
     * Supprime un document (BDD + MinIO).
     */
    @Transactional
    public void deleteDocument(Long documentId) {
        VehiculeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
        s3StorageService.deleteFile(document.getS3Key());
        documentRepository.delete(document);
        log.info("Document supprimé: id={}", documentId);
    }

    private DocumentResponse toResponse(VehiculeDocument doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .vehiculeId(doc.getVehiculeId())
                .nom(doc.getNom())
                .type(doc.getType())
                .taille(doc.getTaille())
                .s3Key(doc.getS3Key())
                .typeDocument(doc.getTypeDocument().name())
                .numero(doc.getNumero())
                .expiration(doc.getExpiration())
                .statut(doc.getStatut().name())
                .dateUpload(doc.getDateUpload())
                .build();
    }
}
