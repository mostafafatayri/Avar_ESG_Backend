package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.model.SecurityRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SecurityRoleRepository
        extends JpaRepository<SecurityRole, Long> {

    boolean existsByCode(String code);

    Optional<SecurityRole> findByCode(String code);

    @EntityGraph(attributePaths = "permissions")
    Optional<SecurityRole> findWithPermissionsById(Long id);

    @EntityGraph(attributePaths = "permissions")
    Optional<SecurityRole> findWithPermissionsByCode(String code);

    List<SecurityRole> findAllByActiveTrueOrderByNameAsc();
}