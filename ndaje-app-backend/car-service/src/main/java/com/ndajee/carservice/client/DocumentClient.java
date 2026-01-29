package com.ndajee.carservice.client;

import com.ndajee.carservice.dto.DocumentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "document-service")
public interface DocumentClient {

    @PostMapping(value = "/api/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    DocumentResponse uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam("entityId") String entityId,
            @RequestParam("entityType") String entityType,
            @RequestParam("typeDocument") String typeDocument,
            @RequestParam("numero") String numero,
            @RequestParam(value = "expiration", required = false) String expiration);

    @GetMapping("/api/documents")
    List<DocumentResponse> getDocumentsByEntity(
            @RequestParam("entityId") String entityId,
            @RequestParam("entityType") String entityType);
}
