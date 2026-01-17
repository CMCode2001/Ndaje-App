package com.ndajee.documentservice.controller;

import com.ndajee.documentservice.dto.DocumentResponse;
import com.ndajee.documentservice.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Upload a document
     * POST /api/documents?utilisateurId=xxx
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("utilisateurId") String utilisateurId) {
        
        log.info("Upload request: file={}, user={}", file.getOriginalFilename(), utilisateurId);
        DocumentResponse response = documentService.uploadDocument(file, utilisateurId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Download a document
     * GET /api/documents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        log.info("Download request: documentId={}", id);
        
        DocumentResponse metadata = documentService.getDocumentMetadata(id);
        byte[] fileData = documentService.downloadDocument(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(metadata.getType()));
        headers.setContentDispositionFormData("attachment", metadata.getNom());
        headers.setContentLength(fileData.length);

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    /**
     * Get document metadata
     * GET /api/documents/{id}/metadata
     */
    @GetMapping("/{id}/metadata")
    public ResponseEntity<DocumentResponse> getDocumentMetadata(@PathVariable Long id) {
        log.info("Metadata request: documentId={}", id);
        DocumentResponse response = documentService.getDocumentMetadata(id);
        return ResponseEntity.ok(response);
    }

    /**
     * List all documents
     * GET /api/documents
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments(
            @RequestParam(required = false) String utilisateurId) {
        
        log.info("List documents request: utilisateurId={}", utilisateurId);
        
        List<DocumentResponse> documents = utilisateurId != null
                ? documentService.getDocumentsByUser(utilisateurId)
                : documentService.getAllDocuments();
        
        return ResponseEntity.ok(documents);
    }

    /**
     * Delete a document
     * DELETE /api/documents/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        log.info("Delete request: documentId={}", id);
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
