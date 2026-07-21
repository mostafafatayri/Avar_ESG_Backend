package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Long> {

    boolean existsByCode(String code);

    List<Site> findByLocationId(Long locationId);
}