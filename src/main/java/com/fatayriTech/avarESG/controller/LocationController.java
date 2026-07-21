package com.fatayriTech.avarESG.controller;

import com.fatayriTech.avarESG.dto.request.LocationRequests.CreateLocationRequest;
import com.fatayriTech.avarESG.dto.request.LocationRequests.CreateSiteRequest;
import com.fatayriTech.avarESG.dto.request.LocationRequests.UpdateLocationRequest;
import com.fatayriTech.avarESG.dto.request.LocationRequests.UpdateSiteRequest;
import com.fatayriTech.avarESG.dto.response.LocationResponse.LocationResponse;
import com.fatayriTech.avarESG.dto.response.LocationResponse.SiteResponse;
import com.fatayriTech.avarESG.service.LocationService.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    @PreAuthorize("hasAuthority('LOCATION_VIEW')")
    public List<LocationResponse> getAllLocations() {
        return locationService.getAllLocations();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCATION_VIEW')")
    public LocationResponse getLocationById(
            @PathVariable Long id
    ) {
        return locationService.getLocationById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('LOCATION_CREATE')")
    public LocationResponse createLocation(
            @Valid @RequestBody
            CreateLocationRequest request
    ) {
        return locationService.createLocation(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCATION_UPDATE')")
    public LocationResponse updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody
            UpdateLocationRequest request
    ) {
        return locationService.updateLocation(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCATION_DELETE')")
    public void deleteLocation(
            @PathVariable Long id
    ) {
        locationService.deleteLocation(id);
    }

    @PostMapping("/{locationId}/sites")
    @PreAuthorize("hasAuthority('SITE_CREATE')")
    public SiteResponse addSite(
            @PathVariable Long locationId,
            @Valid @RequestBody
            CreateSiteRequest request
    ) {
        return locationService.addSite(
                locationId,
                request
        );
    }

    @GetMapping("/{locationId}/sites")
    @PreAuthorize("hasAuthority('SITE_VIEW')")
    public List<SiteResponse> getSitesByLocation(
            @PathVariable Long locationId
    ) {
        return locationService
                .getSitesByLocation(locationId);
    }

    @PutMapping("/{locationId}/sites/{siteId}")
    @PreAuthorize("hasAuthority('SITE_UPDATE')")
    public SiteResponse updateSite(
            @PathVariable Long locationId,
            @PathVariable Long siteId,
            @Valid @RequestBody
            UpdateSiteRequest request
    ) {
        return locationService.updateSite(
                locationId,
                siteId,
                request
        );
    }

    @DeleteMapping("/{locationId}/sites/{siteId}")
    @PreAuthorize("hasAuthority('SITE_DELETE')")
    public void deleteSite(
            @PathVariable Long locationId,
            @PathVariable Long siteId
    ) {
        locationService.deleteSite(
                locationId,
                siteId
        );
    }
}