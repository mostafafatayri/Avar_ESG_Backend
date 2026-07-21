package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.model.InAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InAppNotificationRepository
        extends JpaRepository<InAppNotification, Long> {

    List<InAppNotification>
    findTop30ByUserIdOrderByCreationDateDesc(
            Long userId
    );

    List<InAppNotification>
    findByUserIdOrderByCreationDateDesc(
            Long userId
    );

    long countByUserIdAndReadFalse(
            Long userId
    );

    Optional<InAppNotification>
    findByIdAndUserId(
            Long id,
            Long userId
    );
}