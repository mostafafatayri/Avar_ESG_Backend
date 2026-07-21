package com.fatayriTech.avarESG.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "security_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_security_roles_code",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityRole extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(name = "system_role", nullable = false)
    private boolean systemRole = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(
                    name = "role_id",
                    foreignKey = @ForeignKey(
                            name = "fk_role_permissions_role"
                    )
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "permission_id",
                    foreignKey = @ForeignKey(
                            name = "fk_role_permissions_permission"
                    )
            ),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_role_permission",
                            columnNames = {
                                    "role_id",
                                    "permission_id"
                            }
                    )
            }
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}