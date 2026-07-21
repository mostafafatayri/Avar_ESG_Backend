package com.fatayriTech.avarESG.config;

import com.fatayriTech.avarESG.enums.UserStatus;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.Permission;
import com.fatayriTech.avarESG.model.SecurityRole;
import com.fatayriTech.avarESG.model.UserSecurityRole;
import com.fatayriTech.avarESG.repository.PermissionRepository;
import com.fatayriTech.avarESG.repository.SecurityRoleRepository;
import com.fatayriTech.avarESG.repository.UserRepository;
import com.fatayriTech.avarESG.repository.UserSecurityRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer
        implements CommandLineRunner {

    private static final String SUPER_ADMIN_CODE =
            "SUPER_ADMIN";

    private static final String ADMIN_CODE =
            "ADMIN";

    private static final String ESG_OWNER_CODE =
            "ESG_OWNER";

    private static final String APPROVER_CODE =
            "APPROVER";

    private static final String REVIEWER_CODE =
            "REVIEWER";

    private static final String EXECUTIVE_VIEWER_CODE =
            "EXECUTIVE_VIEWER";

    private final PermissionRepository
            permissionRepository;

    private final SecurityRoleRepository
            securityRoleRepository;

    private final UserRepository
            userRepository;

    private final UserSecurityRoleRepository
            userSecurityRoleRepository;

    private final PasswordEncoder
            passwordEncoder;

    private final AppLoggingProperties
            loggingProperties;

    @Override
    @Transactional
    public void run(
            String... args
    ) {
        log.info(
                "Starting AVAR ESG data initialization"
        );

        Map<String, Permission> permissions =
                seedPermissions();

        Map<String, SecurityRole> roles =
                seedRoles(
                        permissions
                );

        seedSuperAdmin(
                roles.get(
                        SUPER_ADMIN_CODE
                )
        );

        log.info(
                "AVAR ESG data initialization completed"
        );
    }

    /*
     * ============================================================
     * PERMISSIONS
     * ============================================================
     */

    private Map<String, Permission>
    seedPermissions() {

        Map<String, String> definitions =
                buildPermissionDescriptions();

        Map<String, Permission> result =
                new LinkedHashMap<>();

        definitions.forEach(
                (name, description) -> {

                    Permission permission =
                            permissionRepository
                                    .findByName(name)
                                    .orElseGet(
                                            () ->
                                                    Permission.builder()
                                                            .name(name)
                                                            .description(
                                                                    description
                                                            )
                                                            .module(
                                                                    extractModule(
                                                                            name
                                                                    )
                                                            )
                                                            .type(
                                                                    extractType(
                                                                            name
                                                                    )
                                                            )
                                                            .active(true)
                                                            .build()
                                    );

                    /*
                     * Keep existing database records synchronized
                     * with the current permission definitions.
                     */
                    permission.setName(
                            name
                    );

                    permission.setDescription(
                            description
                    );

                    permission.setModule(
                            extractModule(
                                    name
                            )
                    );

                    permission.setType(
                            extractType(
                                    name
                            )
                    );

                    permission.setActive(
                            true
                    );

                    Permission saved =
                            permissionRepository.save(
                                    permission
                            );

                    result.put(
                            saved.getName(),
                            saved
                    );
                }
        );

        log.info(
                "RBAC permissions ready: {}",
                result.size()
        );

        return result;
    }

    /*
     * ============================================================
     * ROLES
     * ============================================================
     */

    private Map<String, SecurityRole>
    seedRoles(
            Map<String, Permission> permissions
    ) {
        Map<String, SecurityRole> roles =
                new HashMap<>();

        /*
         * SUPER ADMIN
         *
         * Developer/platform-level access.
         * Can see and manage the entire application,
         * including users, roles and permissions.
         */
        roles.put(
                SUPER_ADMIN_CODE,
                upsertRole(
                        "Super Administrator",
                        SUPER_ADMIN_CODE,
                        "Full platform, security and development administration access",
                        permissions.values()
                )
        );

        /*
         * ADMIN
         *
         * Current QA requirement:
         * Admin can perform all platform actions.
         */
        roles.put(
                ADMIN_CODE,
                upsertRole(
                        "Administrator",
                        ADMIN_CODE,
                        "Full operational and administrative platform access",
                        permissions.values()
                )
        );

        /*
         * ESG OWNER
         *
         * Can view, create, update, delete eligible records
         * and submit records in the operational ESG modules.
         *
         * Cannot approve or reject.
         *
         * Multiple ESG Owners may manage the same records.
         * createdBy/modifiedBy and future audit logs identify
         * who performed each operation.
         */
        roles.put(
                ESG_OWNER_CODE,
                upsertRole(
                        "ESG Owner",
                        ESG_OWNER_CODE,
                        "Creates, edits and submits ESG operational data",
                        filterPermissions(
                                permissions,
                                this::isEsgOwnerPermission
                        )
                )
        );

        /*
         * APPROVER
         *
         * Can see the same business modules as ESG Owner,
         * but cannot create, update or delete.
         *
         * Can approve, reject and comment.
         */
        roles.put(
                APPROVER_CODE,
                upsertRole(
                        "Approver",
                        APPROVER_CODE,
                        "Reviews, approves and rejects ESG submissions",
                        filterPermissions(
                                permissions,
                                this::isApproverPermission
                        )
                )
        );

        /*
         * REVIEWER
         *
         * Standard read-only application user.
         * Can view, comment and export supported information.
         */
        roles.put(
                REVIEWER_CODE,
                upsertRole(
                        "Reviewer",
                        REVIEWER_CODE,
                        "Views, comments on and exports ESG information",
                        filterPermissions(
                                permissions,
                                this::isReviewerPermission
                        )
                )
        );

        /*
         * EXECUTIVE VIEWER
         *
         * Read-only executive visibility.
         * Can view dashboards, KPIs, objectives,
         * initiatives, Carbon, reports and contextual
         * site/location information.
         */
        roles.put(
                EXECUTIVE_VIEWER_CODE,
                upsertRole(
                        "Executive Viewer",
                        EXECUTIVE_VIEWER_CODE,
                        "Read-only executive access to ESG performance information",
                        filterPermissions(
                                permissions,
                                this::isExecutiveViewerPermission
                        )
                )
        );

        log.info(
                "RBAC roles ready: {}",
                roles.size()
        );

        return roles;
    }

    /*
     * ============================================================
     * ROLE PERMISSION RULES
     * ============================================================
     */

    private boolean isEsgOwnerPermission(
            Permission permission
    ) {
        if (permission == null
                || !permission.isActive()) {

            return false;
        }

        /*
         * ESG Owners need read-only access to location/site
         * records to choose facilities in Carbon, KPI,
         * Objective and Initiative forms.
         */
        if (matchesModule(
                permission,
                "DASHBOARD",
                "LOCATION",
                "SITE"
        )) {
            return permission.getType()
                    .equals("VIEW");
        }

        /*
         * Main modules controlled by ESG Owners.
         */
        if (!matchesModule(
                permission,
                "KPI",
                "KPI_READING",
                "OBJECTIVE",
                "INITIATIVE",
                "CARBON"
        )) {
            return false;
        }

        return Set.of(
                "VIEW",
                "CREATE",
                "UPDATE",
                "DELETE",
                "SUBMIT",
                "COMMENT",
                "EXPORT"
        ).contains(
                permission.getType()
        );
    }

    private boolean isApproverPermission(
            Permission permission
    ) {
        if (permission == null
                || !permission.isActive()) {

            return false;
        }

        /*
         * Approver can open the dashboard and use locations
         * and sites as contextual filters.
         */
        if (matchesModule(
                permission,
                "DASHBOARD",
                "LOCATION",
                "SITE"
        )) {
            return permission.getType()
                    .equals("VIEW");
        }

        /*
         * Approver sees the same ESG business modules as
         * ESG Owner, but only receives review permissions.
         */
        if (!matchesModule(
                permission,
                "KPI",
                "KPI_READING",
                "OBJECTIVE",
                "INITIATIVE",
                "CARBON"
        )) {
            return false;
        }

        return Set.of(
                "VIEW",
                "APPROVE",
                "REJECT",
                "COMMENT",
                "EXPORT"
        ).contains(
                permission.getType()
        );
    }

    private boolean isReviewerPermission(
            Permission permission
    ) {
        if (permission == null
                || !permission.isActive()) {

            return false;
        }

        if (matchesModule(
                permission,
                "LOCATION",
                "SITE"
        )) {
            return permission.getType()
                    .equals("VIEW");
        }

        if (!matchesModule(
                permission,
                "DASHBOARD",
                "KPI",
                "KPI_READING",
                "OBJECTIVE",
                "INITIATIVE",
                "CARBON",
                "REPORT"
        )) {
            return false;
        }

        return Set.of(
                "VIEW",
                "COMMENT",
                "EXPORT"
        ).contains(
                permission.getType()
        );
    }

    private boolean isExecutiveViewerPermission(
            Permission permission
    ) {
        if (permission == null
                || !permission.isActive()) {

            return false;
        }

        if (!permission.getType()
                .equals("VIEW")
                &&
                !permission.getType()
                        .equals("EXPORT")) {

            return false;
        }

        return matchesModule(
                permission,
                "DASHBOARD",
                "KPI",
                "KPI_READING",
                "OBJECTIVE",
                "INITIATIVE",
                "CARBON",
                "REPORT",
                "LOCATION",
                "SITE"
        );
    }

    private SecurityRole upsertRole(
            String name,
            String code,
            String description,
            Collection<Permission> permissions
    ) {
        SecurityRole role =
                securityRoleRepository
                        .findWithPermissionsByCode(
                                code
                        )
                        .orElseGet(
                                () ->
                                        SecurityRole.builder()
                                                .name(name)
                                                .code(code)
                                                .description(
                                                        description
                                                )
                                                .systemRole(
                                                        true
                                                )
                                                .active(
                                                        true
                                                )
                                                .permissions(
                                                        new HashSet<>()
                                                )
                                                .build()
                        );

        role.setName(
                name
        );

        role.setCode(
                code
        );

        role.setDescription(
                description
        );

        role.setSystemRole(
                true
        );

        role.setActive(
                true
        );

        /*
         * Replacing the complete set keeps the database
         * synchronized with this role definition.
         */
        role.setPermissions(
                new HashSet<>(
                        permissions
                )
        );

        SecurityRole saved =
                securityRoleRepository.save(
                        role
                );

        if (loggingProperties.isVerbose()) {
            log.info(
                    "Role ready: code={}, permissionCount={}",
                    saved.getCode(),
                    saved.getPermissions().size()
            );
        }

        return saved;
    }

    /*
     * ============================================================
     * SUPER ADMIN USER
     * ============================================================
     */

    private void seedSuperAdmin(
            SecurityRole superAdminRole
    ) {
        if (superAdminRole == null) {
            throw new IllegalStateException(
                    "SUPER_ADMIN role was not initialized"
            );
        }

        String username =
                "superadmin";

        String email =
                "admin@avaresg.com";

        AppUser user =
                userRepository
                        .findByUsernameIgnoreCase(
                                username
                        )
                        .orElseGet(
                                () ->
                                        AppUser.builder()
                                                .fullName(
                                                        "System Admin"
                                                )
                                                .username(
                                                        username
                                                )
                                                .email(
                                                        email
                                                )
                                                .password(
                                                        passwordEncoder.encode(
                                                                "Admin@123"
                                                        )
                                                )
                                                .jobTitle(
                                                        "Super Administrator"
                                                )
                                                .businessUnit(
                                                        "AVAR ESG"
                                                )
                                                .siteFacility(
                                                        "Head Office"
                                                )
                                                .status(
                                                        UserStatus.ACTIVE
                                                )
                                                .build()
                        );

        user.setFullName(
                "System Admin"
        );

        user.setUsername(
                username
        );

        user.setEmail(
                email
        );

        user.setJobTitle(
                "Super Administrator"
        );

        user.setBusinessUnit(
                "AVAR ESG"
        );

        user.setSiteFacility(
                "Head Office"
        );

        user.setStatus(
                UserStatus.ACTIVE
        );

        /*
         * Never reset an existing valid password
         * during application startup.
         */
        if (user.getPassword() == null
                || user.getPassword()
                .isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(
                            "Admin@123"
                    )
            );
        }

        AppUser savedUser =
                userRepository.save(
                        user
                );

        boolean mappingExists =
                userSecurityRoleRepository
                        .existsByUserIdAndRoleId(
                                savedUser.getId(),
                                superAdminRole.getId()
                        );

        if (!mappingExists) {
            UserSecurityRole mapping =
                    UserSecurityRole.builder()
                            .userId(
                                    savedUser.getId()
                            )
                            .role(
                                    superAdminRole
                            )
                            .build();

            userSecurityRoleRepository.save(
                    mapping
            );
        }

        log.info(
                "Super administrator ready: username={}, email={}",
                username,
                email
        );

        if (loggingProperties.isVerbose()) {
            log.info(
                    "Super admin role mapping ready: userId={}, roleId={}",
                    savedUser.getId(),
                    superAdminRole.getId()
            );
        }
    }

    /*
     * ============================================================
     * FILTER HELPERS
     * ============================================================
     */

    private Collection<Permission>
    filterPermissions(
            Map<String, Permission> permissions,
            Predicate<Permission> predicate
    ) {
        return permissions.values()
                .stream()
                .filter(
                        predicate
                )
                .collect(
                        Collectors.toSet()
                );
    }

    private boolean matchesModule(
            Permission permission,
            String... modules
    ) {
        Set<String> allowedModules =
                Set.of(
                        modules
                );

        return allowedModules.contains(
                permission.getModule()
        );
    }

    /*
     * Examples:
     *
     * KPI_VIEW               -> KPI
     * KPI_READING_VIEW       -> KPI_READING
     * OBJECTIVE_APPROVE      -> OBJECTIVE
     * CARBON_SUBMIT          -> CARBON
     */
    private String extractModule(
            String permissionName
    ) {
        int separator =
                permissionName.lastIndexOf(
                        '_'
                );

        if (separator <= 0) {
            return "GENERAL";
        }

        return permissionName.substring(
                0,
                separator
        );
    }

    /*
     * Examples:
     *
     * KPI_VIEW               -> VIEW
     * KPI_READING_APPROVE    -> APPROVE
     * CARBON_SUBMIT          -> SUBMIT
     */
    private String extractType(
            String permissionName
    ) {
        int separator =
                permissionName.lastIndexOf(
                        '_'
                );

        if (separator < 0
                || separator
                == permissionName.length() - 1) {

            return "GENERAL";
        }

        return permissionName.substring(
                separator + 1
        );
    }

    /*
     * ============================================================
     * PERMISSION DEFINITIONS
     * ============================================================
     */

    private Map<String, String>
    buildPermissionDescriptions() {

        Map<String, String> permissions =
                new LinkedHashMap<>();

        /*
         * Administration
         */
        addCrud(
                permissions,
                "USER",
                "users"
        );

        addCrud(
                permissions,
                "ROLE",
                "security roles"
        );

        permissions.put(
                "ROLE_PERMISSION_MANAGE",
                "Manage role permissions"
        );

        permissions.put(
                "PERMISSION_VIEW",
                "View platform permissions"
        );

        permissions.put(
                "PERMISSION_MANAGE",
                "Manage platform permissions"
        );

        permissions.put(
                "USER_ROLE_MANAGE",
                "Assign and remove user roles"
        );

        /*
         * Location and facilities
         */
        addCrud(
                permissions,
                "LOCATION",
                "locations"
        );

        addCrud(
                permissions,
                "SITE",
                "sites and facilities"
        );

        /*
         * Dashboard
         */
        permissions.put(
                "DASHBOARD_VIEW",
                "View ESG dashboard"
        );

        /*
         * KPI lifecycle
         */
        addCrud(
                permissions,
                "KPI",
                "ESG KPIs"
        );

        permissions.put(
                "KPI_SUBMIT",
                "Submit KPI for approval"
        );

        permissions.put(
                "KPI_APPROVE",
                "Approve pending KPI"
        );

        permissions.put(
                "KPI_REJECT",
                "Reject pending KPI"
        );

        permissions.put(
                "KPI_ARCHIVE",
                "Archive KPI"
        );

        permissions.put(
                "KPI_COMMENT",
                "Comment on KPI"
        );

        /*
         * KPI readings
         */
        addCrud(
                permissions,
                "KPI_READING",
                "KPI readings"
        );

        permissions.put(
                "KPI_READING_SUBMIT",
                "Submit KPI reading for approval"
        );

        permissions.put(
                "KPI_READING_APPROVE",
                "Approve KPI reading"
        );

        permissions.put(
                "KPI_READING_REJECT",
                "Reject KPI reading"
        );

        permissions.put(
                "KPI_READING_COMMENT",
                "Comment on KPI reading"
        );

        /*
         * Objectives
         */
        addCrud(
                permissions,
                "OBJECTIVE",
                "ESG objectives"
        );

        permissions.put(
                "OBJECTIVE_SUBMIT",
                "Submit ESG objective for approval"
        );

        permissions.put(
                "OBJECTIVE_APPROVE",
                "Approve ESG objective"
        );

        permissions.put(
                "OBJECTIVE_REJECT",
                "Reject ESG objective"
        );

        permissions.put(
                "OBJECTIVE_COMMENT",
                "Comment on ESG objective"
        );

        /*
         * Initiatives
         */
        addCrud(
                permissions,
                "INITIATIVE",
                "ESG initiatives"
        );

        permissions.put(
                "INITIATIVE_SUBMIT",
                "Submit ESG initiative for approval"
        );

        permissions.put(
                "INITIATIVE_APPROVE",
                "Approve ESG initiative"
        );

        permissions.put(
                "INITIATIVE_REJECT",
                "Reject ESG initiative"
        );

        permissions.put(
                "INITIATIVE_COMMENT",
                "Comment on ESG initiative"
        );

        /*
         * Carbon
         */
        addCrud(
                permissions,
                "CARBON",
                "carbon emission records"
        );

        permissions.put(
                "CARBON_SUBMIT",
                "Submit carbon emission for approval"
        );

        permissions.put(
                "CARBON_APPROVE",
                "Approve carbon emission"
        );

        permissions.put(
                "CARBON_REJECT",
                "Reject carbon emission"
        );

        permissions.put(
                "CARBON_COMMENT",
                "Comment on carbon emission"
        );

        /*
         * Reporting
         */
        permissions.put(
                "REPORT_VIEW",
                "View ESG reports"
        );

        permissions.put(
                "REPORT_EXPORT",
                "Export ESG reports"
        );

        /*
         * Additional existing platform modules.
         *
         * Super Admin and Admin receive these.
         * They are not currently assigned to ESG Owner,
         * Approver, Reviewer or Executive Viewer by the
         * role filters above.
         */
        addCrud(
                permissions,
                "AUDIT",
                "ESG audits"
        );

        permissions.put(
                "AUDIT_APPROVE",
                "Approve audit results"
        );

        permissions.put(
                "AUDIT_REJECT",
                "Reject audit results"
        );

        permissions.put(
                "AUDIT_COMMENT",
                "Comment on audits"
        );

        addCrud(
                permissions,
                "COMPLIANCE",
                "compliance records"
        );

        addCrud(
                permissions,
                "RISK",
                "ESG risks"
        );

        addCrud(
                permissions,
                "DOCUMENT",
                "ESG documents"
        );

        /*
         * Notifications
         */
        permissions.put(
                "NOTIFICATION_RULE_VIEW",
                "View notification rules"
        );

        permissions.put(
                "NOTIFICATION_RULE_CREATE",
                "Create notification rules"
        );

        permissions.put(
                "NOTIFICATION_RULE_UPDATE",
                "Update notification rules"
        );

        permissions.put(
                "NOTIFICATION_RULE_DELETE",
                "Delete notification rules"
        );

        permissions.put(
                "NOTIFICATION_EVENT_VIEW",
                "View notification events"
        );

        return permissions;
    }

    private void addCrud(
            Map<String, String> permissions,
            String module,
            String resourceName
    ) {
        permissions.put(
                module + "_VIEW",
                "View " + resourceName
        );

        permissions.put(
                module + "_CREATE",
                "Create " + resourceName
        );

        permissions.put(
                module + "_UPDATE",
                "Update " + resourceName
        );

        permissions.put(
                module + "_DELETE",
                "Delete " + resourceName
        );
    }
}