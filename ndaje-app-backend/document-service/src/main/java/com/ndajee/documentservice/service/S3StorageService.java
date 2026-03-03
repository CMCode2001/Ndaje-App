package com.ndajee.documentservice.service;

import com.ndajee.documentservice.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

/**
 * Service de bas niveau gérant les interactions directes avec le stockage
 * MinIO (API compatible S3).
 * S'occupe de l'upload, du téléchargement et de la suppression physique des
 * fichiers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${minio.bucket}")
    private String bucketName;

    /**
     * Upload un fichier vers MinIO et génère une clé unique.
     * 
     * @param file          Fichier multipart à uploader
     * @param utilisateurId ID de l'utilisateur pour l'organisation des dossiers
     *                      dans MinIO
     * @return Clé unique de l'objet stocké (S3 Key)
     * @throws StorageException en cas d'erreur de lecture ou d'accès S3
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
        } catch (SdkException e) {
            String errorMessage = (e instanceof S3Exception s3e && s3e.awsErrorDetails() != null)
                    ? s3e.awsErrorDetails().errorMessage()
                    : e.getMessage();
            log.error("AWS SDK error while uploading file {}: {}", s3Key, errorMessage, e);
            throw new StorageException("Erreur lors de l'upload vers S3: " + errorMessage, e);
        }
    }

    /**
     * Télécharge le contenu d'un fichier depuis MinIO.
     * 
     * @param s3Key Clé unique du fichier
     * @return Tableau d'octets contenant les données du fichier
     * @throws StorageException si le fichier n'existe pas ou en cas d'erreur réseau
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
        } catch (SdkException e) {
            String errorMessage = (e instanceof S3Exception s3e && s3e.awsErrorDetails() != null)
                    ? s3e.awsErrorDetails().errorMessage()
                    : e.getMessage();
            log.error("AWS SDK error while downloading file {}: {}", s3Key, errorMessage, e);
            throw new StorageException("Erreur lors du téléchargement depuis S3: " + errorMessage, e);
        }
    }

    /**
     * Supprime un fichier du stockage MinIO.
     * 
     * @param s3Key Clé unique du fichier à supprimer
     * @throws StorageException en cas d'échec de la suppression
     */
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", s3Key);

        } catch (SdkException e) {
            String errorMessage = (e instanceof S3Exception s3e && s3e.awsErrorDetails() != null)
                    ? s3e.awsErrorDetails().errorMessage()
                    : e.getMessage();
            log.error("AWS SDK error while deleting file {}: {}", s3Key, errorMessage, e);
            throw new StorageException("Erreur lors de la suppression du fichier S3: " + errorMessage, e);
        }
    }
}
