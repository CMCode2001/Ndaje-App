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

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final S3StorageService s3StorageService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

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

    public byte[] downloadDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        return s3StorageService.downloadFile(document.getUrlS3());
    }

    public DocumentResponse getDocumentMetadata(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        return mapToResponse(document);
    }

    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DocumentResponse> getDocumentsByUser(String utilisateurId) {
        return documentRepository.findByUtilisateurId(utilisateurId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

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
