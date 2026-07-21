package com.fatayriTech.avarESG.dto.response.LocationResponse;

import com.fatayriTech.avarESG.enums.LocationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LocationResponse {

    private Long id;
    private String name;
    private String code;
    private String address;
    private String countryCode;
    private String countryName;
    private String countryFlag;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String managerName;
    private String managerEmail;
    private LocationStatus status;
    private int sitesCount;
    private List<SiteResponse> sites;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}