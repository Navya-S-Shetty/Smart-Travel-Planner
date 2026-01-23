package com.tripplanner.repository;

import com.tripplanner.entity.Landmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandmarkRepository extends JpaRepository<Landmark, Long> {

    // MAIN method used by itinerary planner
    List<Landmark> findByCityIgnoreCase(String city);
    
    List<Landmark> findByRegionAndCategoryIgnoreCase(String region, String category);

}
