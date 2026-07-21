package com.fatayriTech.avarESG.service.AuthService;

import com.fatayriTech.avarESG.config.AppLoggingProperties;
import com.fatayriTech.avarESG.dto.request.UserRequests.CreateUserRequest;
import com.fatayriTech.avarESG.dto.request.UserRequests.UpdateUserRequest;
import com.fatayriTech.avarESG.dto.response.UserResponse.UserResponse;
import com.fatayriTech.avarESG.enums.UserStatus;
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.exceptions.ResourceNotFoundException;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.Permission;
import com.fatayriTech.avarESG.model.SecurityRole;
import com.fatayriTech.avarESG.model.UserSecurityRole;
import com.fatayriTech.avarESG.repository.SecurityRoleRepository;
import com.fatayriTech.avarESG.repository.UserRepository;
import com.fatayriTech.avarESG.repository.UserSecurityRoleRepository;
import com.fatayriTech.avarESG.service.EmailService.EmailQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AppLoggingProperties loggingProperties;
    private final PasswordEncoder passwordEncoder;
    private final SecurityRoleRepository securityRoleRepository;
    private final UserSecurityRoleRepository userSecurityRoleRepository;
    private final EmailQueueService emailQueueService;
    @Override
    @Transactional
    public UserResponse createUser(
            CreateUserRequest request
    ) {
        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        String username =
                request.getUsername()
                        .trim()
                        .toLowerCase();

        if (userRepository
                .existsByEmailIgnoreCase(email)) {
            throw new BadRequestException(
                    "Email already exists"
            );
        }

        if (userRepository
                .existsByUsernameIgnoreCase(
                        username
                )) {
            throw new BadRequestException(
                    "Username already exists"
            );
        }

        String rawPassword =
                request.getPassword();

        AppUser user =
                AppUser.builder()
                        .fullName(
                                request.getFullName()
                                        .trim()
                        )
                        .username(username)
                        .email(email)
                        .password(
                                passwordEncoder.encode(
                                        rawPassword
                                )
                        )
                        .jobTitle(
                                request.getJobTitle()
                        )
                        .departmentId(
                                request.getDepartmentId()
                        )
                        .businessUnit(
                                request.getBusinessUnit()
                        )
                        .siteFacility(
                                request.getSiteFacility()
                        )
                        .status(
                                request.getStatus() != null
                                        ? request.getStatus()
                                        : UserStatus.ACTIVE
                        )
                        .build();

        AppUser savedUser =
                userRepository.save(user);

        Set<String> roleCodes =
                request.getRoleCodes();

        if (roleCodes == null ||
                roleCodes.isEmpty()) {
            roleCodes =
                    Set.of(
                            "EXECUTIVE_VIEWER"
                    );
        }

        replaceUserRoles(
                savedUser.getId(),
                roleCodes
        );

        String assignedRoles =
                String.join(", ", roleCodes);

        String subject =
                "Your AVAR ESG account has been created";

        String body =
                """
                Hello %s,
    
                Your AVAR ESG account has been created.
    
                Username: %s
                Email: %s
                Password: %s
                Assigned roles: %s
    
                Login URL: http://localhost:5173/login
    
                Please change your password after your first login.
    
                Regards,
                AVAR ESG Team
                """
                        .formatted(
                                savedUser.getFullName(),
                                savedUser.getUsername(),
                                savedUser.getEmail(),
                                rawPassword,
                                assignedRoles
                        );

        emailQueueService.queueEmail(
                savedUser.getEmail(),
                subject,
                body,
                "USER_ACCOUNT_CREATED",
                savedUser.getId(),
                null
        );

        return mapToResponse(savedUser);
    }
    @Override
    public List<UserResponse> getAllUsers() {
        if (loggingProperties.isVerbose()) {
            log.info("Fetching all users");
        }

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        if (loggingProperties.isVerbose()) {
            log.info("Fetching user by id: {}", id);
        }

        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return mapToResponse(user);
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        if (loggingProperties.isVerbose()) {
            log.info("Updating user with id: {}", id);
        }

        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
                throw new BadRequestException("Username already exists");
            }
            user.setUsername(request.getUsername());
        }
        if (request.getRoleCodes() != null) {
            replaceUserRoles(
                    user.getId(),
                    request.getRoleCodes()
            );
        }

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getJobTitle() != null) user.setJobTitle(request.getJobTitle());
       // if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getDepartmentId() != null) user.setDepartmentId(request.getDepartmentId());
        if (request.getBusinessUnit() != null) user.setBusinessUnit(request.getBusinessUnit());
        if (request.getSiteFacility() != null) user.setSiteFacility(request.getSiteFacility());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        AppUser updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (loggingProperties.isVerbose()) {
            log.warn("Deleting user with id: {}", id);
        }

        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }
    private void replaceUserRoles(
            Long userId,
            Set<String> roleCodes
    ) {
        userSecurityRoleRepository.deleteByUserId(userId);

        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }

        for (String roleCode : roleCodes) {
            SecurityRole role = securityRoleRepository
                    .findByCode(roleCode.trim().toUpperCase())
                    .orElseThrow(() ->
                            new BadRequestException(
                                    "Role not found: " + roleCode
                            )
                    );

            if (!role.isActive()) {
                throw new BadRequestException(
                        "Role is inactive: " + role.getCode()
                );
            }

            UserSecurityRole mapping =
                    UserSecurityRole.builder()
                            .userId(userId)
                            .role(role)
                            .build();

            userSecurityRoleRepository.save(mapping);
        }
    }
    private UserResponse mapToResponse(AppUser user) {
        List<UserSecurityRole> mappings =
                userSecurityRoleRepository.findByUserId(user.getId());

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

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .jobTitle(user.getJobTitle())
                .departmentId(user.getDepartmentId())
                .businessUnit(user.getBusinessUnit())
                .siteFacility(user.getSiteFacility())
                .status(user.getStatus())
                .mustChangePassword(
                        user.isMustChangePassword()
                )
                .roles(roles)
                .permissions(permissions)
                .createdAt(
                        user.getCreationDate()
                )
                .updatedAt(
                        user.getModifiedDate()
                )
                .lastLoginAt(
                        user.getLastLoginAt()
                )
                .build();
    }
}