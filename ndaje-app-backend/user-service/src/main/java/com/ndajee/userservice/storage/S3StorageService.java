package com.ndajee.userservice.storage;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${minio.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String userId) {
        if (file.isEmpty())
            throw new IllegalArgumentException("Le fichier est vide");

        String originalFilename = file.getOriginalFilename();
        String ext = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        // Les fichiers utilisateurs sont préfixés par 'users/' pour les distinguer des
        // fichiers véhicules
        String s3Key = String.format("users/%s/%s%s", userId, UUID.randomUUID(), ext);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucketName).key(s3Key)
                            .contentType(file.getContentType()).build(),
                    RequestBody.fromBytes(file.getBytes()));
            log.info("Fichier utilisateur uploadé : {}", s3Key);
            return s3Key;
        } catch (IOException e) {
            throw new StorageException("Erreur de lecture du fichier", e);
        } catch (SdkException e) {
            String msg = (e instanceof S3Exception s3e && s3e.awsErrorDetails() != null)
                    ? s3e.awsErrorDetails().errorMessage()
                    : e.getMessage();
            throw new StorageException("Erreur upload MinIO: " + msg, e);
        }
    }

    public byte[] downloadFile(String s3Key) {
        try {
            return s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucketName).key(s3Key).build()).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new StorageException("Fichier introuvable: " + s3Key);
        } catch (SdkException e) {
            String msg = (e instanceof S3Exception s3e && s3e.awsErrorDetails() != null)
                    ? s3e.awsErrorDetails().errorMessage()
                    : e.getMessage();
            throw new StorageException("Erreur téléchargement: " + msg, e);
        }
    }

    public void deleteFile(String s3Key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(s3Key).build());
            log.info("Fichier supprimé de MinIO: {}", s3Key);
        } catch (SdkException e) {
            String msg = (e instanceof S3Exception s3e && s3e.awsErrorDetails() != null)
                    ? s3e.awsErrorDetails().errorMessage()
                    : e.getMessage();
            throw new StorageException("Erreur suppression: " + msg, e);
        }
    }
}
