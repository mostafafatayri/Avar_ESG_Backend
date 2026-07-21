package com.fatayriTech.avarESG.controller;

import com.fatayriTech.avarESG.dto.response.LocationResponse.SiteResponse;
import com.fatayriTech.avarESG.service.LocationService.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/sites")
@RequiredArgsConstructor
public class SiteController {

    private final LocationService locationService;

    @GetMapping
    @PreAuthorize("hasAuthority('SITE_VIEW')")
    public List<SiteResponse> getAllSites() {
        return locationService.getAllSites();
    }
}