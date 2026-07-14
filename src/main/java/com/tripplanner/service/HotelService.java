package com.tripplanner.service;

import com.tripplanner.entity.Hotel;
import com.tripplanner.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    public List<Hotel> getHotelsByRegion(String region) {
        return hotelRepository.findByRegionIgnoreCase(region);
    }

    public List<Hotel> getHotelsByRegionAndCategory(String region, String category) {
        return hotelRepository.findByRegionIgnoreCaseAndCategoryIgnoreCase(region, category);
    }
}
