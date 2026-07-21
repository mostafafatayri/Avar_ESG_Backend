package com.fatayriTech.avarESG.model;

import com.fatayriTech.avarESG.enums.SiteStatus;
import com.fatayriTech.avarESG.enums.SiteType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_name", nullable = false)
    private String name;

    @Column(name = "site_code", nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "site_type", nullable = false)
    private SiteType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SiteStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
}