package com.ndajee.documentservice.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(String message) {
        super(message);
    }

    public DocumentNotFoundException(Long id) {
        super("Document non trouvé avec l'ID: " + id);
    }
}
