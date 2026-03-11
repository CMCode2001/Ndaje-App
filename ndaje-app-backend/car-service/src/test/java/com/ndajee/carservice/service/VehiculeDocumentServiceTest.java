package com.ndajee.carservice.service;

import com.ndajee.carservice.domain.StatutDocument;
import com.ndajee.carservice.domain.TypeDocumentVehicule;
import com.ndajee.carservice.domain.VehiculeDocument;
import com.ndajee.carservice.dto.DocumentResponse;
import com.ndajee.carservice.exception.ResourceNotFoundException;
import com.ndajee.carservice.repository.VehiculeDocumentRepository;
import com.ndajee.carservice.storage.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculeDocumentServiceTest {

    @Mock
    private VehiculeDocumentRepository documentRepository;

    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private VehiculeDocumentService documentService;

    private MockMultipartFile file;
    private VehiculeDocument document;

    @BeforeEach
    void setUp() {
        file = new MockMultipartFile("file", "certicat.pdf", "application/pdf", "content".getBytes());

        document = VehiculeDocument.builder()
                .id(1L)
                .vehiculeId(10L)
                .nom("certicat.pdf")
                .type("application/pdf")
                .taille((long) "content".getBytes().length)
                .s3Key("10/uuid-certicat.pdf")
                .typeDocument(TypeDocumentVehicule.ASSURANCE)
                .numero("12345")
                .expiration(LocalDate.of(2025, 12, 31))
                .statut(StatutDocument.SOUMIS)
                .build();
    }

    @Test
    void uploadDocument_ShouldSuccess() {
        when(s3StorageService.uploadFile(any(MultipartFile.class), anyString())).thenReturn("10/uuid-certicat.pdf");
        when(documentRepository.save(any(VehiculeDocument.class))).thenReturn(document);

        DocumentResponse response = documentService.uploadDocument(10L, file, "ASSURANCE", "12345", "2025-12-31");

        assertNotNull(response);
        assertEquals("ASSURANCE", response.getTypeDocument());
        assertEquals("12345", response.getNumero());
        verify(s3StorageService, times(1)).uploadFile(file, "10");
        verify(documentRepository, times(1)).save(any(VehiculeDocument.class));
    }

    @Test
    void uploadDocument_EmptyFile_ShouldThrowIllegalArgument() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> documentService.uploadDocument(10L, emptyFile, "ASSURANCE", "123", null));
        verify(s3StorageService, never()).uploadFile(any(), any());
    }

    @Test
    void uploadDocument_TooLargeFile_ShouldThrowIllegalArgument() {
        byte[] largeData = new byte[10 * 1024 * 1024 + 1]; // 10MB + 1 byte
        MockMultipartFile largeFile = new MockMultipartFile("file", "large.pdf", "application/pdf", largeData);

        assertThrows(IllegalArgumentException.class,
                () -> documentService.uploadDocument(10L, largeFile, "ASSURANCE", "123", null));
        verify(s3StorageService, never()).uploadFile(any(), any());
    }

    @Test
    void getDocumentsByVehicule_ShouldReturnList() {
        when(documentRepository.findByVehiculeId(10L)).thenReturn(List.of(document));

        List<DocumentResponse> list = documentService.getDocumentsByVehicule(10L);

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
    }

    @Test
    void downloadDocument_ShouldReturnBytes() {
        byte[] content = "file-content".getBytes();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(s3StorageService.downloadFile("10/uuid-certicat.pdf")).thenReturn(content);

        byte[] result = documentService.downloadDocument(1L);

        assertNotNull(result);
        assertArrayEquals(content, result);
    }

    @Test
    void downloadDocument_ShouldThrowException_WhenNotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentService.downloadDocument(1L));
        verify(s3StorageService, never()).downloadFile(anyString());
    }

    @Test
    void deleteDocument_ShouldDeleteFromRepoAndS3() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        doNothing().when(s3StorageService).deleteFile("10/uuid-certicat.pdf");

        documentService.deleteDocument(1L);

        verify(s3StorageService, times(1)).deleteFile("10/uuid-certicat.pdf");
        verify(documentRepository, times(1)).delete(document);
    }
}
