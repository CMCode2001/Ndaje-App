package com.ndajee.documentservice.service;

import com.ndajee.documentservice.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Upload file to S3 and return the S3 object key
     */
    public String uploadFile(MultipartFile file, String utilisateurId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        // Generate unique S3 key: userId/uuid-filename
        String s3Key = String.format("%s/%s%s", utilisateurId, UUID.randomUUID(), fileExtension);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            
            log.info("File uploaded successfully to S3: {}", s3Key);
            return s3Key;
            
        } catch (IOException e) {
            log.error("Failed to read file: {}", originalFilename, e);
            throw new StorageException("Erreur lors de la lecture du fichier", e);
        } catch (S3Exception e) {
            log.error("S3 error while uploading file: {}", s3Key, e);
            throw new StorageException("Erreur lors de l'upload vers S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * Download file from S3
     */
    public byte[] downloadFile(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            byte[] data = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
            log.info("File downloaded successfully from S3: {}", s3Key);
            return data;
            
        } catch (NoSuchKeyException e) {
            log.error("File not found in S3: {}", s3Key);
            throw new StorageException("Fichier introuvable dans S3: " + s3Key);
        } catch (S3Exception e) {
            log.error("S3 error while downloading file: {}", s3Key, e);
            throw new StorageException("Erreur lors du téléchargement depuis S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * Delete file from S3
     */
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", s3Key);
            
        } catch (S3Exception e) {
            log.error("S3 error while deleting file: {}", s3Key, e);
            throw new StorageException("Erreur lors de la suppression du fichier S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }
}
