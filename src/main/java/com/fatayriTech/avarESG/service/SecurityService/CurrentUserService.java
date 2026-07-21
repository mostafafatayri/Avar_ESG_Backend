package com.fatayriTech.avarESG.service.SecurityService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrentUserService {

    public CurrentUser getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof CurrentUser currentUser)) {
            throw new IllegalStateException(
                    "Authenticated user was not found"
            );
        }

        return currentUser;
    }

    public Long getUserId() {
        return getCurrentUser().userId();
    }

    public String getEmail() {
        return getCurrentUser().email();
    }

    public String getUsername() {
        return getCurrentUser().username();
    }

    public String getFullName() {
        return getCurrentUser().fullName();
    }

    public List<String> getRoles() {
        return getCurrentUser().roles();
    }

    public List<String> getPermissions() {
        return getCurrentUser().permissions();
    }
}