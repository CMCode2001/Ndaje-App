package com.ndajee.carservice.storage;

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
 * Service bas niveau gérant les interactions directes avec MinIO (API S3).
 * Intégré directement dans car-service — plus besoin d'appel Feign vers
 * document-service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${minio.bucket}")
    private String bucketName;

    /**
     * Upload un fichier vers MinIO et retourne la clé unique générée.
     *
     * @param file     Fichier multipart à uploader
     * @param entityId ID de l'entité propriétaire (ex: vehiculeId)
     * @return Clé S3 unique de l'objet stocké
     */
    public String uploadFile(MultipartFile file, String entityId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String s3Key = String.format("vehicules/%s/%s%s", entityId, UUID.randomUUID(), fileExtension);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("Fichier uploadé vers MinIO: {}", s3Key);
            return s3Key;

        } catch (IOException e) {
            log.error("Erreur de lecture du fichier: {}", originalFilename, e);
            throw new StorageException("Erreur lors de la lecture du fichier", e);
        } catch (SdkException e) {
            String errorMsg = (e instanceof S3Exception s3e && s3e.awsErrorDetails() != null)
                    ? s3e.awsErrorDetails().errorMessage()
                    : e.getMessage();
            log.error("Erreur SDK lors de l'upload {}: {}", s3Key, errorMsg, e);
            throw new StorageException("Erreur lors de l'upload vers MinIO: " + errorMsg, e);
        }
    }

    /**
     * Télécharge le contenu binaire d'un fichier depuis MinIO.
     */
    public byte[] downloadFile(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            byte[] data = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
            log.info("Fichier téléchargé depuis MinIO: {}", s3Key);
            return data;

        } catch (NoSuchKeyException e) {
            log.error("Fichier introuvable dans MinIO: {}", s3Key);
            throw new StorageException("Fichier introuvable: " + s3Key);
        } catch (SdkException e) {
            String errorMsg = (e instanceof S3Exception s3e && s3e.awsErrorDetails() != null)
                    ? s3e.awsErrorDetails().errorMessage()
                    : e.getMessage();
            throw new StorageException("Erreur lors du téléchargement: " + errorMsg, e);
        }
    }

    /**
     * Supprime un fichier de MinIO.
     */
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Fichier supprimé de MinIO: {}", s3Key);

        } catch (SdkException e) {
            String errorMsg = (e instanceof S3Exception s3e && s3e.awsErrorDetails() != null)
                    ? s3e.awsErrorDetails().errorMessage()
                    : e.getMessage();
            throw new StorageException("Erreur lors de la suppression: " + errorMsg, e);
        }
    }
}
