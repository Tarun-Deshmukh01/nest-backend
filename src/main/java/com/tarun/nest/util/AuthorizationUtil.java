package com.tarun.nest.util;

import com.tarun.nest.security.JwtAuthenticationDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationUtil {

    /**
     * Verify that the authenticated user owns the resource (userId match)
     * @param authentication The authentication object
     * @param resourceUserId The user ID of the resource owner
     * @return true if the user owns the resource, false otherwise
     */
    public boolean isOwner(Authentication authentication, Long resourceUserId) {
        if (authentication == null || !(authentication.getDetails() instanceof JwtAuthenticationDetails)) {
            return false;
        }
        
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        return details.getUserId().equals(resourceUserId);
    }

    /**
     * Verify that the authenticated vendor owns the product/resource (vendorId match)
     * @param authentication The authentication object
     * @param resourceVendorId The vendor ID of the resource owner
     * @return true if the vendor owns the resource, false otherwise
     */
    public boolean isVendorOwner(Authentication authentication, Long resourceVendorId) {
        if (authentication == null || !(authentication.getDetails() instanceof JwtAuthenticationDetails)) {
            return false;
        }
        
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        return details.getUserId().equals(resourceVendorId);
    }

    /**
     * Check if the authenticated user has a specific role
     * @param authentication The authentication object
     * @param role The role to check (without ROLE_ prefix)
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || !(authentication.getDetails() instanceof JwtAuthenticationDetails)) {
            return false;
        }
        
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        return details.getRole().equals(role);
    }

    /**
     * Get the authenticated user ID
     * @param authentication The authentication object
     * @return The user ID, or null if not authenticated
     */
    public Long getUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof JwtAuthenticationDetails)) {
            return null;
        }
        
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        return details.getUserId();
    }

    /**
     * Get the authenticated user email
     * @param authentication The authentication object
     * @return The email, or null if not authenticated
     */
    public String getEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        
        return authentication.getName();
    }

    /**
     * Get the authenticated user role
     * @param authentication The authentication object
     * @return The role, or null if not authenticated
     */
    public String getRole(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof JwtAuthenticationDetails)) {
            return null;
        }
        
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        return details.getRole();
    }
}
