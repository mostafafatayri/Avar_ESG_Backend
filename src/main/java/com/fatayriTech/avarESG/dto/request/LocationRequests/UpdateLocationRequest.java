package com.fatayriTech.avarESG.dto.request.LocationRequests;

import com.fatayriTech.avarESG.enums.LocationStatus;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateLocationRequest {

    private String name;

    private String code;

    private String address;

    private String countryCode;

    private String countryName;

    private String countryFlag;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String managerName;

    @Email
    private String managerEmail;

    private LocationStatus status;
}