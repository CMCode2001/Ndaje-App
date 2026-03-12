package com.ndajee.userservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import com.ndajee.userservice.exception.BusinessException;

public class SecurityUtils {

    /**
     * Extracts the user ID (usually 'sub') from the current JWT token.
     * Throws BusinessException if no authenticated user is found.
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getName(); // Usually 'sub' claim
        }
        throw new BusinessException("User is not authenticated");
    }

    /**
     * Verifies that the currently authenticated user is the owner of the resource
     * being accessed. Throws BusinessException (usually map to 403 Forbidden) if
     * unauthorized.
     * 
     * @param resourceOwnerId The ID of the user who owns the resource.
     */
    public static void verifyOwnership(String resourceOwnerId) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(resourceOwnerId)) {
            throw new BusinessException("Access Denied: You are not authorized to access or modify this resource.");
        }
    }
}
