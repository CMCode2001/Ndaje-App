package com.ndajee.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "document-service")
public interface DocumentClient {

    @GetMapping("/api/documents/user/{userId}")
    List<Object> getDocumentsByUserId(@PathVariable("userId") String userId);
}
