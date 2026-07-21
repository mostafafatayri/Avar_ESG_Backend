package com.fatayriTech.avarESG.model;

import com.fatayriTech.avarESG.enums.LocationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_name", nullable = false)
    private String name;

    @Column(name = "location_code", nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String address;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "country_name", nullable = false)
    private String countryName;

    @Column(name = "country_flag")
    private String countryFlag;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "manager_email")
    private String managerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationStatus status;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Site> sites = new ArrayList<>();
}