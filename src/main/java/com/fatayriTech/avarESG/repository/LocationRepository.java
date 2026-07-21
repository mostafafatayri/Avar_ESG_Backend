package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

    boolean existsByCode(String code);
}