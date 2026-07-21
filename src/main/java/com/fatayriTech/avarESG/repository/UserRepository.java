package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.enums.UserStatus;
import com.fatayriTech.avarESG.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByUsernameIgnoreCase(String username);

    Optional<AppUser> findByEmailIgnoreCaseOrUsernameIgnoreCase(
            String email,
            String username
    );

    // =========================
    // Notification Engine
    // =========================

    List<AppUser> findByStatus(UserStatus status);

    List<AppUser> findByDepartmentIdAndStatus(
            Long departmentId,
            UserStatus status
    );
}