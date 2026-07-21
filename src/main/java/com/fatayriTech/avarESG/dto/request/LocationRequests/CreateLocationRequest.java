package com.fatayriTech.avarESG.dto.request.LocationRequests;

import com.fatayriTech.avarESG.enums.SiteType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateLocationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    @NotBlank
    private String address;

    @NotBlank
    private String countryCode;

    @NotBlank
    private String countryName;

    private String countryFlag;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

    private String managerName;

    @Email
    private String managerEmail;

    private String initialSiteName;

    private String initialSiteCode;

    private SiteType initialSiteType;
}