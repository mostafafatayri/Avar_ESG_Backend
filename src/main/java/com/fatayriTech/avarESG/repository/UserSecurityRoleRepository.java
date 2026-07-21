package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.model.UserSecurityRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserSecurityRoleRepository
        extends JpaRepository<UserSecurityRole, Long> {

    @EntityGraph(attributePaths = {
            "role",
            "role.permissions"
    })
    List<UserSecurityRole> findByUserId(Long userId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    long countByRoleId(Long roleId);

    @Transactional
    void deleteByUserIdAndRoleId(Long userId, Long roleId);

    @Transactional
    void deleteByUserId(Long userId);



    @Query("""
            SELECT mapping.userId
            FROM UserSecurityRole mapping
            WHERE UPPER(mapping.role.code) =
                  UPPER(:roleCode)
              AND mapping.role.active = true
            """)
    List<Long> findUserIdsByRoleCode(
            @Param("roleCode")
            String roleCode
    );
}