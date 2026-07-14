package com.tripplanner.service;

import com.tripplanner.entity.Landmark;
import com.tripplanner.repository.LandmarkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LandmarkService {

    @Autowired
    private LandmarkRepository landmarkRepository;

    
    public Landmark saveLandmark(Landmark landmark) {
        return landmarkRepository.save(landmark);
    }

    
    public List<Landmark> getLandmarksByCity(String city) {
        return landmarkRepository.findByCityIgnoreCase(city);
    }
}
