package com.ndajee.userservice.service;

import com.ndajee.userservice.dto.DocumentUtilisateurResponse;
import com.ndajee.userservice.entities.StatutDocument;
import com.ndajee.userservice.entities.TypeDocumentUtilisateur;
import com.ndajee.userservice.entities.UtilisateurDocument;
import com.ndajee.userservice.exception.BusinessException;
import com.ndajee.userservice.repositories.UtilisateurDocumentRepository;
import com.ndajee.userservice.storage.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service local pour la gestion des documents d'un utilisateur.
 * Remplace la dépendance Feign vers document-service.
 * Les fichiers sont stockés dans MinIO sous le préfixe 'users/{userId}/'.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDocumentService {

    private final UtilisateurDocumentRepository documentRepository;
    private final S3StorageService s3StorageService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Upload un document justificatif pour un utilisateur.
     */
    @Transactional
    public DocumentUtilisateurResponse uploadDocument(String userId, MultipartFile file,
            String typeDocStr, String numero,
            String expirationStr) {
        if (file.isEmpty())
            throw new IllegalArgumentException("Le fichier est vide");
        if (file.getSize() > MAX_FILE_SIZE)
            throw new IllegalArgumentException("Fichier trop volumineux (max 10MB)");

        String s3Key = s3StorageService.uploadFile(file, userId);

        TypeDocumentUtilisateur type;
        try {
            type = TypeDocumentUtilisateur.valueOf(typeDocStr.toUpperCase());
        } catch (Exception e) {
            log.warn("Type inconnu '{}', classé comme AUTRE", typeDocStr);
            type = TypeDocumentUtilisateur.AUTRE;
        }

        LocalDate expiration = null;
        if (expirationStr != null && !expirationStr.isBlank()) {
            try {
                expiration = LocalDate.parse(expirationStr);
            } catch (Exception e) {
                log.warn("Date d'expiration invalide: {}", expirationStr);
            }
        }

        UtilisateurDocument doc = UtilisateurDocument.builder()
                .userId(userId)
                .nom(file.getOriginalFilename())
                .contentType(file.getContentType())
                .taille(file.getSize())
                .s3Key(s3Key)
                .typeDocument(type)
                .numero(numero)
                .expiration(expiration)
                .statut(StatutDocument.SOUMIS)
                .build();

        UtilisateurDocument saved = documentRepository.save(doc);
        log.info("Document uploadé pour l'utilisateur {}: id={}, type={}", userId, saved.getId(), type);
        return toResponse(saved);
    }

    /**
     * Liste tous les documents d'un utilisateur.
     */
    @Transactional(readOnly = true)
    public List<DocumentUtilisateurResponse> getDocumentsByUser(String userId) {
        return documentRepository.findByUserId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Télécharge le contenu binaire d'un document.
     */
    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long documentId) {
        UtilisateurDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException("Document introuvable: " + documentId));
        return s3StorageService.downloadFile(doc.getS3Key());
    }

    /**
     * Supprime un document (BDD + MinIO).
     */
    @Transactional
    public void deleteDocument(Long documentId) {
        UtilisateurDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException("Document introuvable: " + documentId));
        s3StorageService.deleteFile(doc.getS3Key());
        documentRepository.delete(doc);
        log.info("Document supprimé: id={}", documentId);
    }

    private DocumentUtilisateurResponse toResponse(UtilisateurDocument doc) {
        return DocumentUtilisateurResponse.builder()
                .id(doc.getId())
                .userId(doc.getUserId())
                .nom(doc.getNom())
                .contentType(doc.getContentType())
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
