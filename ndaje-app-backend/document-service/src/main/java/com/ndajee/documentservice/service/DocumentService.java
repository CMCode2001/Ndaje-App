package com.ndajee.documentservice.service;

import com.ndajee.documentservice.dto.DocumentResponse;
import com.ndajee.documentservice.entity.Document;
import com.ndajee.documentservice.exception.DocumentNotFoundException;
import com.ndajee.documentservice.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service métier orchestrant la gestion des documents.
 * Combine les opérations de base de données (métadonnées) et le stockage Cloudflare R2 (fichiers).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final S3StorageService s3StorageService;

    /** Taille maximale autorisée : 10 Mo */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Effectue l'upload complet d'un document : stockage physique puis enregistrement en base.
     * @param file Le fichier à uploader
     * @param utilisateurId L'ID du propriétaire
     * @return Les métadonnées du document sauvegardé
     */
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, String utilisateurId) {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("La taille du fichier dépasse 10MB");
        }

        // Upload to S3
        String s3Key = s3StorageService.uploadFile(file, utilisateurId);

        // Save metadata to database
        Document document = new Document();
        document.setNom(file.getOriginalFilename());
        document.setUrlS3(s3Key);
        document.setType(file.getContentType());
        document.setTaille(file.getSize());
        document.setUtilisateurId(utilisateurId);

        Document saved = documentRepository.save(document);
        log.info("Document metadata saved: id={}, user={}", saved.getId(), utilisateurId);

        return mapToResponse(saved);
    }

    /**
     * Récupère le contenu binaire d'un document.
     * @param id Identifiant technique du document
     * @return Données du fichier
     */
    public byte[] downloadDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        return s3StorageService.downloadFile(document.getUrlS3());
    }

    /**
     * Récupère les métadonnées d'un document par son ID.
     */
    public DocumentResponse getDocumentMetadata(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        return mapToResponse(document);
    }

    /**
     * Liste tous les documents enregistrés dans le système.
     */
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Liste les documents appartenant à un utilisateur spécifique.
     */
    public List<DocumentResponse> getDocumentsByUser(String utilisateurId) {
        return documentRepository.findByUtilisateurId(utilisateurId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Supprime un document : physique (R2) et logique (BDD).
     * @param id ID du document à supprimer
     */
    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        // Delete from S3
        s3StorageService.deleteFile(document.getUrlS3());

        // Delete from database
        documentRepository.delete(document);
        log.info("Document deleted: id={}, s3Key={}", id, document.getUrlS3());
    }

    private DocumentResponse mapToResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getNom(),
                document.getType(),
                document.getTaille(),
                document.getDateUpload(),
                document.getUtilisateurId(),
                document.getUrlS3()
        );
    }
}
