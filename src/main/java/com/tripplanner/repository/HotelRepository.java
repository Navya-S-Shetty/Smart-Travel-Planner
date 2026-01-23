package com.tripplanner.repository;

import com.tripplanner.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // Get hotels by region (cluster)
    List<Hotel> findByRegionIgnoreCase(String region);

    // Get hotels by region + category (budget / midrange / luxury)
    List<Hotel> findByRegionIgnoreCaseAndCategoryIgnoreCase(String region, String category);
}
