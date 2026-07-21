package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.enums.NotificationEventType;
import com.fatayriTech.avarESG.enums.NotificationModule;
import com.fatayriTech.avarESG.model.NotificationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRuleRepository
        extends JpaRepository<NotificationRule, Long> {

    Optional<NotificationRule> findByCodeIgnoreCase(
            String code
    );

    boolean existsByCodeIgnoreCase(
            String code
    );

    boolean existsByCodeIgnoreCaseAndIdNot(
            String code,
            Long id
    );

    List<NotificationRule>
    findAllByOrderByCreationDateDesc();

    List<NotificationRule>
    findByActiveTrueOrderByCreationDateDesc();

    List<NotificationRule>
    findByModuleAndEventTypeAndActiveTrue(
            NotificationModule module,
            NotificationEventType eventType
    );
}