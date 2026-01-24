package com.ndajee.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "document-service")
public interface DocumentClient {

    @GetMapping("/api/documents")
    List<Object> getDocumentsByUserId(
            @RequestParam("entityId") String userId,
            @RequestParam("entityType") String entityType);
}
