package com.tripplanner.controller;
import org.springframework.security.core.Authentication;
import com.tripplanner.dto.ItineraryResponse;
import com.tripplanner.dto.SaveItineraryRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import com.tripplanner.service.ItineraryService;
import java.util.Map;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/itinerary")
public class ItineraryController {

    @Autowired
    private ItineraryService itineraryService;
    @PostMapping("/generate")
    public Map<String, Object> generate(
    		 @RequestParam String city,
    	        @RequestParam String category,
    	        @RequestParam String pace,
    	        @RequestParam String start,
    	        @RequestParam String end,
    	        @RequestParam String companion,
    	        @RequestParam(required = false) String hotelType,
    	        @RequestParam String travelMode   

    ) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();

        System.out.println("LOGGED IN USER = " + username);

        return itineraryService.buildFinalItinerary(
            city, category, pace, start, end, companion, hotelType ,  travelMode
        );
    }
    
    @PostMapping("/save")
    public void saveTrip(@RequestBody SaveItineraryRequest request) {
        itineraryService.saveItinerary(
            request.getTripName(),
            request.getItineraryJson()
        );
    }
    
    @GetMapping("/my-trips")
    public List<ItineraryResponse> getMyTrips(Authentication authentication) {

        return itineraryService.getTripsForUser(authentication.getName())
                .stream()
                .map(i -> new ItineraryResponse(
                        i.getId(),
                        i.getTripName(),
                        i.getGeneratedPlan()
                ))
                .collect(java.util.stream.Collectors.toList());

    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTrip(@PathVariable Long id) {
        itineraryService.deleteTrip(id);
        return ResponseEntity.ok().build();
    }





}
