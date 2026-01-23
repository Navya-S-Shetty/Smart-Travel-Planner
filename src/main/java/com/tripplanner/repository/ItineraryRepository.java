package com.tripplanner.repository;

import com.tripplanner.entity.Itinerary;
import com.tripplanner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    List<Itinerary> findByUser(User user);

}


