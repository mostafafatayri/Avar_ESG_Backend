package com.fatayriTech.avarESG.dto.request.LocationRequests;

import com.fatayriTech.avarESG.enums.SiteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSiteRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    @NotNull
    private SiteType type;
}