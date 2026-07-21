package com.fatayriTech.avarESG.service.LocationService;

import com.fatayriTech.avarESG.config.AppLoggingProperties;
import com.fatayriTech.avarESG.dto.request.LocationRequests.CreateLocationRequest;
import com.fatayriTech.avarESG.dto.request.LocationRequests.CreateSiteRequest;
import com.fatayriTech.avarESG.dto.request.LocationRequests.UpdateLocationRequest;
import com.fatayriTech.avarESG.dto.request.LocationRequests.UpdateSiteRequest;
import com.fatayriTech.avarESG.dto.response.LocationResponse.LocationResponse;
import com.fatayriTech.avarESG.dto.response.LocationResponse.SiteResponse;
import com.fatayriTech.avarESG.enums.LocationStatus;
import com.fatayriTech.avarESG.enums.SiteStatus;
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.exceptions.ResourceNotFoundException;
import com.fatayriTech.avarESG.model.Location;
import com.fatayriTech.avarESG.model.Site;
import com.fatayriTech.avarESG.repository.LocationRepository;
import com.fatayriTech.avarESG.repository.SiteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final SiteRepository siteRepository;
    private final AppLoggingProperties loggingProperties;

    @Override
    public LocationResponse createLocation(CreateLocationRequest request) {
        if (loggingProperties.isVerbose()) {
            log.info("Creating location with code: {}", request.getCode());
        }

        if (locationRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Location code already exists");
        }

        Location location = Location.builder()
                .name(request.getName())
                .code(request.getCode())
                .address(request.getAddress())
                .countryCode(request.getCountryCode())
                .countryName(request.getCountryName())
                .countryFlag(request.getCountryFlag())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .managerName(request.getManagerName())
                .managerEmail(request.getManagerEmail())
                .status(LocationStatus.ACTIVE)
                .build();

        if (request.getInitialSiteName() != null && !request.getInitialSiteName().isBlank()) {
            if (request.getInitialSiteCode() == null || request.getInitialSiteCode().isBlank()) {
                throw new BadRequestException("Initial site code is required");
            }

            if (request.getInitialSiteType() == null) {
                throw new BadRequestException("Initial site type is required");
            }

            if (siteRepository.existsByCode(request.getInitialSiteCode())) {
                throw new BadRequestException("Initial site code already exists");
            }

            Site site = Site.builder()
                    .name(request.getInitialSiteName())
                    .code(request.getInitialSiteCode())
                    .type(request.getInitialSiteType())
                    .status(SiteStatus.ACTIVE)
                    .location(location)
                    .build();

            location.getSites().add(site);
        }

        Location savedLocation = locationRepository.save(location);

        return mapLocationToResponse(savedLocation);
    }

    @Override
    public List<LocationResponse> getAllLocations() {
        if (loggingProperties.isVerbose()) {
            log.info("Fetching all locations");
        }

        return locationRepository.findAll()
                .stream()
                .map(this::mapLocationToResponse)
                .toList();
    }

    @Override
    public LocationResponse getLocationById(Long id) {
        if (loggingProperties.isVerbose()) {
            log.info("Fetching location by id: {}", id);
        }

        Location location = getLocationEntity(id);

        return mapLocationToResponse(location);
    }

    @Override
    public LocationResponse updateLocation(Long id, UpdateLocationRequest request) {
        if (loggingProperties.isVerbose()) {
            log.info("Updating location with id: {}", id);
        }

        Location location = getLocationEntity(id);

        if (request.getCode() != null && !request.getCode().equals(location.getCode())) {
            if (locationRepository.existsByCode(request.getCode())) {
                throw new BadRequestException("Location code already exists");
            }
            location.setCode(request.getCode());
        }

        if (request.getName() != null) location.setName(request.getName());
        if (request.getAddress() != null) location.setAddress(request.getAddress());
        if (request.getCountryCode() != null) location.setCountryCode(request.getCountryCode());
        if (request.getCountryName() != null) location.setCountryName(request.getCountryName());
        if (request.getCountryFlag() != null) location.setCountryFlag(request.getCountryFlag());
        if (request.getLatitude() != null) location.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) location.setLongitude(request.getLongitude());
        if (request.getManagerName() != null) location.setManagerName(request.getManagerName());
        if (request.getManagerEmail() != null) location.setManagerEmail(request.getManagerEmail());
        if (request.getStatus() != null) location.setStatus(request.getStatus());

        Location updatedLocation = locationRepository.save(location);

        return mapLocationToResponse(updatedLocation);
    }

    @Override
    public void deleteLocation(Long id) {
        if (loggingProperties.isVerbose()) {
            log.warn("Deleting location with id: {}", id);
        }

        Location location = getLocationEntity(id);

        locationRepository.delete(location);
    }

    @Override
    public SiteResponse addSite(Long locationId, CreateSiteRequest request) {
        if (loggingProperties.isVerbose()) {
            log.info("Adding site with code: {} to location id: {}", request.getCode(), locationId);
        }

        Location location = getLocationEntity(locationId);

        if (siteRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Site code already exists");
        }

        Site site = Site.builder()
                .name(request.getName())
                .code(request.getCode())
                .type(request.getType())
                .status(SiteStatus.ACTIVE)
                .location(location)
                .build();

        Site savedSite = siteRepository.save(site);

        return mapSiteToResponse(savedSite);
    }

    @Override
    public List<SiteResponse> getSitesByLocation(Long locationId) {
        getLocationEntity(locationId);

        return siteRepository.findByLocationId(locationId)
                .stream()
                .map(this::mapSiteToResponse)
                .toList();
    }

    @Override
    public SiteResponse updateSite(Long locationId, Long siteId, UpdateSiteRequest request) {
        if (loggingProperties.isVerbose()) {
            log.info("Updating site id: {} for location id: {}", siteId, locationId);
        }

        getLocationEntity(locationId);

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));

        if (!site.getLocation().getId().equals(locationId)) {
            throw new BadRequestException("Site does not belong to this location");
        }

        if (request.getCode() != null && !request.getCode().equals(site.getCode())) {
            if (siteRepository.existsByCode(request.getCode())) {
                throw new BadRequestException("Site code already exists");
            }
            site.setCode(request.getCode());
        }

        if (request.getName() != null) site.setName(request.getName());
        if (request.getType() != null) site.setType(request.getType());
        if (request.getStatus() != null) site.setStatus(request.getStatus());

        Site updatedSite = siteRepository.save(site);

        return mapSiteToResponse(updatedSite);
    }

    @Override
    public void deleteSite(Long locationId, Long siteId) {
        if (loggingProperties.isVerbose()) {
            log.warn("Deleting site id: {} from location id: {}", siteId, locationId);
        }

        getLocationEntity(locationId);

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));

        if (!site.getLocation().getId().equals(locationId)) {
            throw new BadRequestException("Site does not belong to this location");
        }

        siteRepository.delete(site);
    }

    private Location getLocationEntity(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
    }

    private LocationResponse mapLocationToResponse(Location location) {
        List<SiteResponse> sites = location.getSites()
                .stream()
                .map(this::mapSiteToResponse)
                .toList();

        return LocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .code(location.getCode())
                .address(location.getAddress())
                .countryCode(location.getCountryCode())
                .countryName(location.getCountryName())
                .countryFlag(location.getCountryFlag())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .managerName(location.getManagerName())
                .managerEmail(location.getManagerEmail())
                .status(location.getStatus())
                .sitesCount(sites.size())
                .sites(sites)
                .createdAt(location.getCreationDate())
                .updatedAt(location.getModifiedDate())
                .build();
    }



    private SiteResponse mapSiteToResponse(Site site) {
        return SiteResponse.builder()
                .id(site.getId())
                .name(site.getName())
                .code(site.getCode())
                .type(site.getType())
                .status(site.getStatus())
                .locationId(site.getLocation().getId())
                .createdAt(site.getCreationDate())
                .updatedAt(site.getModifiedDate())
                .build();
    }


    @Override
    public List<SiteResponse> getAllSites() {
        if (loggingProperties.isVerbose()) {
            log.info("Fetching all sites");
        }

        return siteRepository.findAll()
                .stream()
                .map(this::mapSiteToResponse)
                .toList();
    }
}