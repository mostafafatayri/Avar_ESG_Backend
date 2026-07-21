package com.fatayriTech.avarESG.service.LocationService;

import com.fatayriTech.avarESG.dto.request.LocationRequests.CreateLocationRequest;
import com.fatayriTech.avarESG.dto.request.LocationRequests.CreateSiteRequest;
import com.fatayriTech.avarESG.dto.request.LocationRequests.UpdateLocationRequest;
import com.fatayriTech.avarESG.dto.request.LocationRequests.UpdateSiteRequest;
import com.fatayriTech.avarESG.dto.response.LocationResponse.LocationResponse;
import com.fatayriTech.avarESG.dto.response.LocationResponse.SiteResponse;

import java.util.List;

public interface LocationService {

    LocationResponse createLocation(
            CreateLocationRequest request
    );

    List<LocationResponse> getAllLocations();

    LocationResponse getLocationById(
            Long id
    );

    LocationResponse updateLocation(
            Long id,
            UpdateLocationRequest request
    );

    void deleteLocation(
            Long id
    );

    SiteResponse addSite(
            Long locationId,
            CreateSiteRequest request
    );

    List<SiteResponse> getSitesByLocation(
            Long locationId
    );

    List<SiteResponse> getAllSites();

    SiteResponse updateSite(
            Long locationId,
            Long siteId,
            UpdateSiteRequest request
    );

    void deleteSite(
            Long locationId,
            Long siteId
    );
}