package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.enums.NotificationChannel;
import com.fatayriTech.avarESG.enums.NotificationStatus;
import com.fatayriTech.avarESG.model.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationEventRepository
        extends JpaRepository<NotificationEvent, Long> {

    boolean existsByEventKeyAndChannel(
            String eventKey,
            NotificationChannel channel
    );

    Optional<NotificationEvent>
    findByEventKeyAndChannel(
            String eventKey,
            NotificationChannel channel
    );

    List<NotificationEvent>
    findAllByOrderByCreationDateDesc();

    List<NotificationEvent>
    findByStatusOrderByCreationDateDesc(
            NotificationStatus status
    );

    List<NotificationEvent>
    findTop50ByStatusOrderByCreationDateAsc(
            NotificationStatus status
    );

    long countByStatus(
            NotificationStatus status
    );

    long countByChannel(
            NotificationChannel channel
    );
}