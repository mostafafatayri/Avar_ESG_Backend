package com.fatayriTech.avarESG.dto.response.LocationResponse;

import com.fatayriTech.avarESG.enums.SiteStatus;
import com.fatayriTech.avarESG.enums.SiteType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SiteResponse {

    private Long id;
    private String name;
    private String code;
    private SiteType type;
    private SiteStatus status;
    private Long locationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}