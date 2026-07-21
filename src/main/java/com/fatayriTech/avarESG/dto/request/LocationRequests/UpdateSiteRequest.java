package com.fatayriTech.avarESG.dto.request.LocationRequests;

import com.fatayriTech.avarESG.enums.SiteStatus;
import com.fatayriTech.avarESG.enums.SiteType;
import lombok.Data;

@Data
public class UpdateSiteRequest {

    private String name;

    private String code;

    private SiteType type;

    private SiteStatus status;
}