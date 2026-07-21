package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.model.UserInvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInvitationTokenRepository
        extends JpaRepository<
        UserInvitationToken,
        Long
        > {

    Optional<UserInvitationToken>
    findByTokenHashAndRevokedFalse(
            String tokenHash
    );

    List<UserInvitationToken>
    findByUserIdAndRevokedFalse(
            Long userId
    );
}