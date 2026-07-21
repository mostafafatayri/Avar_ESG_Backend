package com.fatayriTech.avarESG.service.SecurityService;

import com.fatayriTech.avarESG.config.AppLoggingProperties;
import com.fatayriTech.avarESG.dto.request.SecurityRequests.CreateRoleRequest;
import com.fatayriTech.avarESG.dto.request.SecurityRequests.UpdateRolePermissionsRequest;
import com.fatayriTech.avarESG.dto.request.SecurityRequests.UpdateRoleRequest;
import com.fatayriTech.avarESG.dto.response.SecurityResponse.PermissionResponse;
import com.fatayriTech.avarESG.dto.response.SecurityResponse.RoleResponse;
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.exceptions.ResourceNotFoundException;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.Permission;
import com.fatayriTech.avarESG.model.SecurityRole;
import com.fatayriTech.avarESG.model.UserSecurityRole;
import com.fatayriTech.avarESG.repository.PermissionRepository;
import com.fatayriTech.avarESG.repository.SecurityRoleRepository;
import com.fatayriTech.avarESG.repository.UserRepository;
import com.fatayriTech.avarESG.repository.UserSecurityRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final SecurityRoleRepository securityRoleRepository;
    private final PermissionRepository permissionRepository;
    private final UserSecurityRoleRepository userSecurityRoleRepository;
    private final UserRepository userRepository;
    private final AppLoggingProperties loggingProperties;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return securityRoleRepository.findAll()
                .stream()
                .map(this::mapRoleToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        SecurityRole role = securityRoleRepository
                .findWithPermissionsById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with id: " + id
                        )
                );

        return mapRoleToResponse(role);
    }

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        String normalizedCode = normalizeCode(request.getCode());

        if (securityRoleRepository.existsByCode(normalizedCode)) {
            throw new BadRequestException(
                    "Role code already exists: " + normalizedCode
            );
        }

        Set<Permission> permissions = resolvePermissions(
                request.getPermissionIds()
        );

        SecurityRole role = SecurityRole.builder()
                .name(request.getName().trim())
                .code(normalizedCode)
                .description(request.getDescription())
                .systemRole(false)
                .active(true)
                .permissions(permissions)
                .build();

        SecurityRole savedRole = securityRoleRepository.save(role);

        if (loggingProperties.isVerbose()) {
            log.info(
                    "Created security role: {} with {} permissions",
                    savedRole.getCode(),
                    savedRole.getPermissions().size()
            );
        }

        return mapRoleToResponse(savedRole);
    }

    @Override
    public RoleResponse updateRole(
            Long id,
            UpdateRoleRequest request
    ) {
        SecurityRole role = getRoleEntity(id);

        if (request.getName() != null) {
            role.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        if (request.getActive() != null) {
            role.setActive(request.getActive());
        }

        return mapRoleToResponse(
                securityRoleRepository.save(role)
        );
    }

    @Override
    public RoleResponse updateRolePermissions(
            Long roleId,
            UpdateRolePermissionsRequest request
    ) {
        SecurityRole role = securityRoleRepository
                .findWithPermissionsById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with id: " + roleId
                        )
                );

        Set<Permission> permissions = resolvePermissions(
                request.getPermissionIds()
        );

        role.setPermissions(permissions);

        SecurityRole savedRole =
                securityRoleRepository.save(role);

        if (loggingProperties.isVerbose()) {
            log.info(
                    "Updated permissions for role: {}",
                    savedRole.getCode()
            );
        }

        return mapRoleToResponse(savedRole);
    }

    @Override
    public void deleteRole(Long id) {
        SecurityRole role = getRoleEntity(id);

        if (role.isSystemRole()) {
            throw new BadRequestException(
                    "System roles cannot be deleted"
            );
        }

        long assignedUsers =
                userSecurityRoleRepository.countByRoleId(id);

        if (assignedUsers > 0) {
            throw new BadRequestException(
                    "Role is assigned to users and cannot be deleted"
            );
        }

        securityRoleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository
                .findAllByActiveTrueOrderByModuleAscNameAsc()
                .stream()
                .map(this::mapPermissionToResponse)
                .toList();
    }

    @Override
    public void assignRoleToUser(
            Long userId,
            Long roleId
    ) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        SecurityRole role = getRoleEntity(roleId);

        if (!role.isActive()) {
            throw new BadRequestException(
                    "Inactive role cannot be assigned"
            );
        }

        if (userSecurityRoleRepository
                .existsByUserIdAndRoleId(userId, roleId)) {
            return;
        }

        UserSecurityRole mapping = UserSecurityRole.builder()
                .userId(user.getId())
                .role(role)
                .build();

        userSecurityRoleRepository.save(mapping);

        if (loggingProperties.isVerbose()) {
            log.info(
                    "Assigned role {} to user {}",
                    role.getCode(),
                    user.getUsername()
            );
        }
    }

    @Override
    public void removeRoleFromUser(
            Long userId,
            Long roleId
    ) {
        userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        getRoleEntity(roleId);

        userSecurityRoleRepository
                .deleteByUserIdAndRoleId(userId, roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getUserRoles(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        return userSecurityRoleRepository.findByUserId(userId)
                .stream()
                .map(UserSecurityRole::getRole)
                .map(this::mapRoleToResponse)
                .toList();
    }

    private SecurityRole getRoleEntity(Long id) {
        return securityRoleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with id: " + id
                        )
                );
    }

    private Set<Permission> resolvePermissions(
            Set<Long> permissionIds
    ) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Permission> permissions =
                permissionRepository.findAllById(permissionIds);

        if (permissions.size() != permissionIds.size()) {
            throw new BadRequestException(
                    "One or more permission IDs are invalid"
            );
        }

        return new HashSet<>(permissions);
    }

    private String normalizeCode(String code) {
        return code.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private RoleResponse mapRoleToResponse(
            SecurityRole role
    ) {
        List<PermissionResponse> permissions =
                role.getPermissions() == null
                        ? List.of()
                        : role.getPermissions()
                        .stream()
                        .map(this::mapPermissionToResponse)
                        .sorted(
                                java.util.Comparator.comparing(
                                        PermissionResponse::getName
                                )
                        )
                        .toList();

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode())
                .description(role.getDescription())
                .systemRole(role.isSystemRole())
                .active(role.isActive())
                .assignedUsersCount(
                        userSecurityRoleRepository
                                .countByRoleId(role.getId())
                )
                .permissionsCount(permissions.size())
                .permissions(permissions)
                .createdAt(role.getCreationDate())
                .updatedAt(role.getModifiedDate())
                .build();
    }

    private PermissionResponse mapPermissionToResponse(
            Permission permission
    ) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .module(permission.getModule())
                .type(permission.getType())
                .active(permission.isActive())
                .build();
    }
}