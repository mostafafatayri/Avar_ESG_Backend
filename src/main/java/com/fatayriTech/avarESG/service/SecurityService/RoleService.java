package com.fatayriTech.avarESG.service.SecurityService;

import com.fatayriTech.avarESG.dto.request.SecurityRequests.CreateRoleRequest;
import com.fatayriTech.avarESG.dto.request.SecurityRequests.UpdateRolePermissionsRequest;
import com.fatayriTech.avarESG.dto.request.SecurityRequests.UpdateRoleRequest;
import com.fatayriTech.avarESG.dto.response.SecurityResponse.PermissionResponse;
import com.fatayriTech.avarESG.dto.response.SecurityResponse.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(Long id);

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRole(Long id, UpdateRoleRequest request);

    RoleResponse updateRolePermissions(
            Long roleId,
            UpdateRolePermissionsRequest request
    );

    void deleteRole(Long id);

    List<PermissionResponse> getAllPermissions();

    void assignRoleToUser(Long userId, Long roleId);

    void removeRoleFromUser(Long userId, Long roleId);

    List<RoleResponse> getUserRoles(Long userId);
}