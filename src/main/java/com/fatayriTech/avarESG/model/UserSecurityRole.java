package com.fatayriTech.avarESG.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_security_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_security_role",
                        columnNames = {
                                "user_id",
                                "role_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_security_roles_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_user_security_roles_role",
                        columnList = "role_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSecurityRole extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "role_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_user_security_roles_role"
            )
    )
    private SecurityRole role;
}