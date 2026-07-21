package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.model.PasswordChangeOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordChangeOtpRepository
        extends JpaRepository<PasswordChangeOtp, Long> {

    Optional<PasswordChangeOtp>
    findTopByUserIdAndUsedFalseOrderByCreationDateDesc(
            Long userId
    );

    @Transactional
    void deleteByUserId(Long userId);
}