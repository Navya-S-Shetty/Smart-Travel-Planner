package com.tripplanner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tripplanner.entity.TouristPlace;

@Repository
public interface TouristPlaceRepository extends JpaRepository<TouristPlace, Integer> {

    List<TouristPlace> findByCity(String city);

}
