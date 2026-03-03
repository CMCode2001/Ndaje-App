package com.ndajee.userservice.web;

import com.ndajee.userservice.dto.DocumentUtilisateurResponse;
import com.ndajee.userservice.service.UserDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Endpoints de gestion des documents d'un utilisateur.
 * Accessible via GET /api/users/{userId}/documents
 */
@RestController
@RequestMapping("/api/users/{userId}/documents")
@RequiredArgsConstructor
public class UserDocumentController {

    private final UserDocumentService userDocumentService;

    /**
     * Upload d'un document justificatif (permis, CNI, passeport...)
     * POST /api/users/{userId}/documents
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUtilisateurResponse> uploadDocument(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("typeDocument") String typeDocument,
            @RequestParam(value = "numero", required = false) String numero,
            @RequestParam(value = "expiration", required = false) String expiration) {

        DocumentUtilisateurResponse response = userDocumentService
                .uploadDocument(userId, file, typeDocument, numero, expiration);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Liste tous les documents d'un utilisateur.
     * GET /api/users/{userId}/documents
     */
    @GetMapping
    public ResponseEntity<List<DocumentUtilisateurResponse>> getDocuments(
            @PathVariable String userId) {
        return ResponseEntity.ok(userDocumentService.getDocumentsByUser(userId));
    }

    /**
     * Téléchargement binaire d'un document.
     * GET /api/users/{userId}/documents/{documentId}/download
     */
    @GetMapping("/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable String userId,
            @PathVariable Long documentId) {
        byte[] content = userDocumentService.downloadDocument(documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document-" + documentId + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    /**
     * Suppression d'un document.
     * DELETE /api/users/{userId}/documents/{documentId}
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable String userId,
            @PathVariable Long documentId) {
        userDocumentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
