package com.fatayriTech.avarESG.service.AuthService;

import com.fatayriTech.avarESG.dto.request.UserRequests.LoginRequest;
import com.fatayriTech.avarESG.dto.response.UserResponse.LoginResponse;
import com.fatayriTech.avarESG.dto.response.UserResponse.UserResponse;
import com.fatayriTech.avarESG.enums.UserStatus;
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.Permission;
import com.fatayriTech.avarESG.model.SecurityRole;
import com.fatayriTech.avarESG.model.UserSecurityRole;
import com.fatayriTech.avarESG.repository.UserRepository;
import com.fatayriTech.avarESG.repository.UserSecurityRoleRepository;
import com.fatayriTech.avarESG.service.SecurityService.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserSecurityRoleRepository userSecurityRoleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String identifier =
                request.getIdentifier() == null
                        ? ""
                        : request.getIdentifier().trim();

        AppUser user = userRepository
                .findByEmailIgnoreCaseOrUsernameIgnoreCase(
                        identifier,
                        identifier
                )
                .orElseThrow(() ->
                        new BadRequestException(
                                "Invalid email/username or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new BadRequestException(
                    "Invalid email/username or password"
            );
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException(
                    "User account is not active"
            );
        }

        String accessToken =
                jwtService.generateAccessToken(user);

        String refreshToken =
                jwtService.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        user.setLastLoginAt(LocalDateTime.now());

        AppUser savedUser =
                userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .mustChangePassword(
                        savedUser.isMustChangePassword()
                )
                .user(mapToResponse(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refresh(
            String refreshToken
    ) {
        if (refreshToken == null ||
                refreshToken.isBlank()) {
            throw new BadRequestException(
                    "Refresh token is missing"
            );
        }

        if (!jwtService.isTokenValid(
                refreshToken
        )) {
            throw new BadRequestException(
                    "Invalid refresh token"
            );
        }

        if (!jwtService.isRefreshToken(
                refreshToken
        )) {
            throw new BadRequestException(
                    "Invalid refresh token"
            );
        }

        String email =
                jwtService.extractEmail(
                        refreshToken
                );

        AppUser user =
                userRepository
                        .findByEmailIgnoreCase(
                                email
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid refresh token"
                                )
                        );

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException(
                    "User account is not active"
            );
        }

        if (user.getRefreshToken() == null ||
                !user.getRefreshToken()
                        .equals(refreshToken)) {
            throw new BadRequestException(
                    "Refresh token has been revoked"
            );
        }

        return LoginResponse.builder()
                .accessToken(
                        jwtService.generateAccessToken(
                                user
                        )
                )
                .refreshToken(refreshToken)
                .mustChangePassword(
                        user.isMustChangePassword()
                )
                .user(mapToResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null ||
                refreshToken.isBlank()) {
            return;
        }

        if (!jwtService.isTokenValid(
                refreshToken
        )) {
            return;
        }

        String email =
                jwtService.extractEmail(
                        refreshToken
                );

        userRepository
                .findByEmailIgnoreCase(email)
                .ifPresent(user -> {
                    user.setRefreshToken(null);
                    userRepository.save(user);
                });
    }

    private UserResponse mapToResponse(
            AppUser user
    ) {
        List<UserSecurityRole> mappings =
                userSecurityRoleRepository
                        .findByUserId(
                                user.getId()
                        );

        List<String> roles =
                mappings.stream()
                        .map(
                                UserSecurityRole::getRole
                        )
                        .filter(
                                SecurityRole::isActive
                        )
                        .map(
                                SecurityRole::getCode
                        )
                        .distinct()
                        .sorted()
                        .toList();

        List<String> permissions =
                mappings.stream()
                        .map(
                                UserSecurityRole::getRole
                        )
                        .filter(
                                SecurityRole::isActive
                        )
                        .flatMap(role ->
                                role.getPermissions()
                                        .stream()
                        )
                        .filter(
                                Permission::isActive
                        )
                        .map(
                                Permission::getName
                        )
                        .distinct()
                        .sorted()
                        .toList();

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .jobTitle(user.getJobTitle())
                .departmentId(
                        user.getDepartmentId()
                )
                .businessUnit(
                        user.getBusinessUnit()
                )
                .siteFacility(
                        user.getSiteFacility()
                )
                .status(user.getStatus())
                .mustChangePassword(
                        user.isMustChangePassword()
                )
                .roles(roles)
                .permissions(permissions)
                .lastLoginAt(
                        user.getLastLoginAt()
                )
                .createdAt(
                        user.getCreationDate()
                )
                .updatedAt(
                        user.getModifiedDate()
                )
                .build();
    }
}