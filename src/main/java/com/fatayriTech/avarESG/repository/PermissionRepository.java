package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    List<Permission> findAllByActiveTrueOrderByModuleAscNameAsc();

    Optional<Permission>
    findByNameIgnoreCase(String name);

    List<Permission>
    findByModuleIgnoreCaseAndActiveTrue(
            String module
    );
}