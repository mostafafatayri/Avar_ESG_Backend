package com.fatayriTech.avarESG.service.SecurityService;

import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.Permission;
import com.fatayriTech.avarESG.model.SecurityRole;
import com.fatayriTech.avarESG.model.UserSecurityRole;
import com.fatayriTech.avarESG.repository.UserSecurityRoleRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String SECRET_KEY =
            "12345678912345678912345678912345";

    private static final long ACCESS_TOKEN_EXPIRATION =
            1000L * 60 * 15;

    private static final long REFRESH_TOKEN_EXPIRATION =
            1000L * 60 * 60 * 24 * 7;

    private final UserSecurityRoleRepository
            userSecurityRoleRepository;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(AppUser user) {
        return generateAccessToken(user);
    }

    @Transactional(readOnly = true)
    public String generateAccessToken(AppUser user) {
        return buildToken(
                user,
                ACCESS_TOKEN_EXPIRATION,
                "ACCESS"
        );
    }

    @Transactional(readOnly = true)
    public String generateRefreshToken(AppUser user) {
        return buildToken(
                user,
                REFRESH_TOKEN_EXPIRATION,
                "REFRESH"
        );
    }

    private String buildToken(
            AppUser user,
            long expiration,
            String tokenType
    ) {
        List<UserSecurityRole> mappings =
                userSecurityRoleRepository.findByUserId(
                        user.getId()
                );

        List<String> roles = mappings.stream()
                .map(UserSecurityRole::getRole)
                .filter(SecurityRole::isActive)
                .map(SecurityRole::getCode)
                .distinct()
                .sorted()
                .toList();

        List<String> permissions = mappings.stream()
                .map(UserSecurityRole::getRole)
                .filter(SecurityRole::isActive)
                .flatMap(role ->
                        role.getPermissions().stream()
                )
                .filter(Permission::isActive)
                .map(Permission::getName)
                .distinct()
                .sorted()
                .toList();

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("tokenType", tokenType)
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("fullName", user.getFullName())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim(
                        "permissions",
                        "ACCESS".equals(tokenType)
                                ? permissions
                                : List.of()
                )
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expiration
                        )
                )
                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return "ACCESS".equals(
                getClaims(token).get(
                        "tokenType",
                        String.class
                )
        );
    }

    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(
                getClaims(token).get(
                        "tokenType",
                        String.class
                )
        );
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId =
                getClaims(token).get("userId");

        return userId == null
                ? null
                : Long.valueOf(userId.toString());
    }

    public String extractUsername(String token) {
        return getClaims(token).get(
                "username",
                String.class
        );
    }

    public String extractFullName(String token) {
        return getClaims(token).get(
                "fullName",
                String.class
        );
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = getClaims(token).get("roles");

        return roles == null
                ? List.of()
                : (List<String>) roles;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        Object permissions =
                getClaims(token).get("permissions");

        return permissions == null
                ? List.of()
                : (List<String>) permissions;
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}