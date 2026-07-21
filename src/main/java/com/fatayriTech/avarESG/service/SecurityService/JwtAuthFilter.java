package com.fatayriTech.avarESG.service.SecurityService;

import com.fatayriTech.avarESG.enums.UserStatus;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        boolean publicAuthEndpoint =
                path.equals("/api/v1/auth/login")
                        || path.equals("/api/v1/auth/refresh")
                        || path.equals("/api/v1/auth/logout");

        boolean documentationEndpoint =
                path.startsWith("/swagger-ui")
                        || path.startsWith("/v3/api-docs");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || publicAuthEndpoint
                || documentationEndpoint) {

            filterChain.doFilter(request, response);
            return;
        }

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)
                || !jwtService.isAccessToken(token)
                || SecurityContextHolder.getContext()
                .getAuthentication() != null) {

            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(token);

        AppUser user = userRepository
                .findByEmailIgnoreCase(email)
                .orElse(null);

        if (user == null
                || user.getStatus() != UserStatus.ACTIVE) {

            filterChain.doFilter(request, response);
            return;
        }

        boolean passwordChangeEndpoint =
                path.startsWith(
                        "/api/v1/auth/password-change"
                );

        if (user.isMustChangePassword()
                && !passwordChangeEndpoint) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            response.setContentType(
                    "application/json"
            );

            response.getWriter().write(
                    """
                    {
                      "message": "Password change is required",
                      "status": 403
                    }
                    """
            );

            return;
        }

        List<String> roles =
                jwtService.extractRoles(token);

        List<String> permissions =
                jwtService.extractPermissions(token);

        CurrentUser currentUser =
                new CurrentUser(
                        user.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getFullName(),
                        roles,
                        permissions
                );

        List<SimpleGrantedAuthority> authorities =
                Stream.concat(
                                permissions.stream(),
                                roles.stream()
                                        .map(role ->
                                                "ROLE_" + role
                                        )
                        )
                        .distinct()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        currentUser,
                        null,
                        authorities
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}