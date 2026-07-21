package com.fatayriTech.avarESG.service.SecurityService;

import java.util.List;

public record CurrentUser(
        Long userId,
        String email,
        String username,
        String fullName,
        List<String> roles,
        List<String> permissions
) {
}