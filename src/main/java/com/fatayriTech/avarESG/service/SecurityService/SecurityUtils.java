package com.fatayriTech.avarESG.service.SecurityService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CurrentUser getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof CurrentUser currentUser)) {
            throw new IllegalStateException(
                    "Current user not found"
            );
        }

        return currentUser;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().userId();
    }

    public static boolean hasAuthority(
            String authority
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(item ->
                        item.getAuthority()
                                .equals(authority)
                );
    }

    public static boolean hasRole(String roleCode) {
        return hasAuthority("ROLE_" + roleCode);
    }
}