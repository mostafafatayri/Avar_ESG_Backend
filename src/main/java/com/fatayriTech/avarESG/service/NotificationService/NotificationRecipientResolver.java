package com.fatayriTech.avarESG.service.NotificationService;

import com.fatayriTech.avarESG.enums.NotificationRecipientType;
import com.fatayriTech.avarESG.enums.UserStatus;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.Kpi;
import com.fatayriTech.avarESG.model.NotificationRule;
import com.fatayriTech.avarESG.repository.UserRepository;
import com.fatayriTech.avarESG.repository.UserSecurityRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationRecipientResolver {

    private final UserRepository userRepository;

    private final UserSecurityRoleRepository
            userSecurityRoleRepository;

    public List<AppUser> resolveRecipients(
            NotificationRule rule,
            Kpi kpi
    ) {
        NotificationRecipientType recipientType =
                rule.getRecipientType() == null
                        ? NotificationRecipientType.KPI_OWNER
                        : rule.getRecipientType();

        return switch (recipientType) {
            case KPI_OWNER ->
                    resolveKpiOwner(kpi);

            case USER ->
                    resolveSpecificUser(rule);

            case ROLE ->
                    resolveRoleUsers(rule);

            case ALL_ACTIVE_USERS ->
                    userRepository.findByStatus(
                            UserStatus.ACTIVE
                    );
        };
    }

    private List<AppUser> resolveKpiOwner(
            Kpi kpi
    ) {
        if (kpi == null ||
                kpi.getResponsibleOwner() == null) {

            return List.of();
        }

        AppUser owner =
                kpi.getResponsibleOwner();

        if (owner.getStatus() !=
                UserStatus.ACTIVE) {

            return List.of();
        }

        return List.of(owner);
    }

    private List<AppUser> resolveSpecificUser(
            NotificationRule rule
    ) {
        if (rule.getRecipientUserId() == null) {
            return List.of();
        }

        return userRepository
                .findById(
                        rule.getRecipientUserId()
                )
                .filter(user ->
                        user.getStatus() ==
                                UserStatus.ACTIVE
                )
                .map(List::of)
                .orElseGet(List::of);
    }

    private List<AppUser> resolveRoleUsers(
            NotificationRule rule
    ) {
        if (rule.getRecipientRoleCode() == null ||
                rule.getRecipientRoleCode()
                        .isBlank()) {

            return List.of();
        }

        List<Long> userIds =
                userSecurityRoleRepository
                        .findUserIdsByRoleCode(
                                rule.getRecipientRoleCode()
                        );

        if (userIds.isEmpty()) {
            return List.of();
        }

        return userRepository.findAllById(
                        userIds
                )
                .stream()
                .filter(user ->
                        user.getStatus() ==
                                UserStatus.ACTIVE
                )
                .toList();
    }
}