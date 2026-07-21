package com.fatayriTech.avarESG.controller;

import com.fatayriTech.avarESG.dto.request.SecurityRequests.AssignRoleRequest;
import com.fatayriTech.avarESG.dto.request.SecurityRequests.CreateRoleRequest;
import com.fatayriTech.avarESG.dto.request.SecurityRequests.UpdateRolePermissionsRequest;
import com.fatayriTech.avarESG.dto.request.SecurityRequests.UpdateRoleRequest;
import com.fatayriTech.avarESG.dto.response.SecurityResponse.PermissionResponse;
import com.fatayriTech.avarESG.dto.response.SecurityResponse.RoleResponse;
import com.fatayriTech.avarESG.service.SecurityService.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public RoleResponse getRoleById(
            @PathVariable Long id
    ) {
        return roleService.getRoleById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public RoleResponse createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        return roleService.createRole(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public RoleResponse updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return roleService.updateRole(id, request);
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_PERMISSION_MANAGE')")
    public RoleResponse updateRolePermissions(
            @PathVariable Long id,
            @Valid @RequestBody
            UpdateRolePermissionsRequest request
    ) {
        return roleService.updateRolePermissions(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public void deleteRole(
            @PathVariable Long id
    ) {
        roleService.deleteRole(id);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public List<PermissionResponse> getAllPermissions() {
        return roleService.getAllPermissions();
    }

    @PostMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('USER_ROLE_MANAGE')")
    public void assignRoleToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        roleService.assignRoleToUser(
                userId,
                request.getRoleId()
        );
    }

    @DeleteMapping("/users/{userId}/{roleId}")
    @PreAuthorize("hasAuthority('USER_ROLE_MANAGE')")
    public void removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        roleService.removeRoleFromUser(
                userId,
                roleId
        );
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('USER_ROLE_MANAGE')")
    public List<RoleResponse> getUserRoles(
            @PathVariable Long userId
    ) {
        return roleService.getUserRoles(userId);
    }
}