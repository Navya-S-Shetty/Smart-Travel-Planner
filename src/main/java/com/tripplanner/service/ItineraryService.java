package com.tripplanner.service;
import com.tripplanner.service.HotelService;




import com.tripplanner.service.WeatherService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tripplanner.entity.Hotel;
import com.tripplanner.entity.Itinerary;
import com.tripplanner.entity.User;
import com.tripplanner.repository.ItineraryRepository;
import com.tripplanner.repository.UserRepository;


import com.tripplanner.entity.Landmark;
import com.tripplanner.repository.LandmarkRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItineraryService {

    @Autowired
    private LandmarkRepository landmarkRepository;
    
    @Autowired
    private HotelService hotelService;

    @Autowired
    private WeatherService weatherService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItineraryRepository itineraryRepository;




    private final RestTemplate restTemplate = new RestTemplate();
    
    
    private double getHotelCost(List<Map<String, Object>> hotels) {

        if (hotels == null || hotels.isEmpty()) return 0;

        Object price = hotels.get(0).get("pricePerNight");

        if (price == null) return 0;

        try {
            return Double.parseDouble(price.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    
    
    private double getEntryFeeTotal(List<Map<String, Object>> stops) {

        double total = 0;

        for (Map<String, Object> stop : stops) {

            Object feeObj = stop.get("entryFee");
            if (feeObj == null) continue;

            String fee = feeObj.toString().toLowerCase().trim();

            // ignore free / permit / budget
            if (fee.contains("free") ||
                fee.contains("permit") ||
                fee.contains("budget") ||
                fee.contains("reasonable")) {
                continue;
            }

            // remove everything except numbers and dash
            fee = fee.replaceAll("[^0-9–-]", "");

            try {

                // range case 100–250
                if (fee.contains("–") || fee.contains("-")) {

                    String[] parts = fee.split("[–-]");
                    double min = Double.parseDouble(parts[0]);
                    double max = Double.parseDouble(parts[1]);

                    total += (min + max) / 2; // average
                } 
                else {
                    // single value ₹10
                    total += Double.parseDouble(fee);
                }

            } catch (Exception e) {
                // skip bad formats
            }
        }

        return total;
    }


    private double getTransportCost(String travelMode, double distanceKm) {

        if (travelMode == null) return 0;

        return switch (travelMode) {
            case "bike" -> distanceKm * 3;
            case "car" -> distanceKm * 7;
            case "public" -> distanceKm * 2;
            default -> 0;
        };
    }

    
    private int getFoodCostPerPerson(String hotelType) {

        if (hotelType == null) return 500;

        hotelType = hotelType.toLowerCase();

        return switch (hotelType) {
            case "budget" -> 300;
            case "mid-range", "midrange", "mid range" -> 500;
            case "premium", "luxury" -> 700;
            default -> 300;
        };
    }


    
    private List<String> orderRegionsByCategoryMatch(
            Map<String, List<Landmark>> regionMap,
            List<String> userCategories
    ) {
        return regionMap.entrySet()
                .stream()
                .sorted((e1, e2) -> {
                    long c1 = e1.getValue().stream()
                            .filter(l -> l.getCategory() != null)
                            .filter(l -> userCategories.contains(l.getCategory().toLowerCase()))
                            .count();

                    long c2 = e2.getValue().stream()
                            .filter(l -> l.getCategory() != null)
                            .filter(l -> userCategories.contains(l.getCategory().toLowerCase()))
                            .count();

                    return Long.compare(c2, c1); // DESCENDING
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

    }


    /* =========================
       MAIN ENTRY METHOD
       ========================= */
    public Map<String, Object> buildFinalItinerary(
            String city,
            String category,
            String pace,
            String start,
            String end,
            String companion,
            String hotelType ,
            String travelMode// ✅ NEW (optional)
    )

 {
    	
    	// ✅ NORMALIZE hotelType (FIX FOR MID-RANGE)
    	// ✅ NORMALIZE hotelType for DB matching
    	
    	boolean userSelectedHotel = false;

    	if (hotelType != null && !hotelType.isBlank()) {

    	    hotelType = hotelType.trim().toLowerCase();

    	    switch (hotelType) {
    	        case "budget":
    	            hotelType = "Budget";
    	            userSelectedHotel = true;
    	            break;

    	        case "mid-range":
    	        case "mid range":
    	        case "midrange":
    	            hotelType = "Mid-range";
    	            userSelectedHotel = true;
    	            break;

    	        case "premium":
    	        case "luxury":
    	            hotelType = "Premium";
    	            userSelectedHotel = true;
    	            break;
    	    }
    	}



        /* -------- 1. DATE & PACE -------- */
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        int placesPerDay = switch (pace.toLowerCase()) {
            case "relaxed" -> 3;
            case "packed" -> 6;
            default -> 4; // balanced
        };

        /* -------- 2. FETCH LANDMARKS -------- */
     // 1️⃣ Fetch landmarks
        List<Landmark> allLandmarks =
                landmarkRepository.findByCityIgnoreCase(city.trim());

        if (allLandmarks.isEmpty()) {
            return Map.of("itinerary", Collections.emptyList());
        }

        // 2️⃣ User categories
        List<String> userCategories = Arrays.stream(category.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        
        
     // 🔁 Copy for rotation (DO NOT MODIFY userCategories directly)
        List<String> rotatedCategories = new ArrayList<>(userCategories);


        // 3️⃣ Group by region
        Map<String, List<Landmark>> regionMap =
                allLandmarks.stream()
                        .collect(Collectors.groupingBy(Landmark::getRegion));

        // 4️⃣ Order regions
        List<String> orderedRegions =
                userCategories.isEmpty()
                ? orderRegionsByDistance(regionMap)
                : orderRegionsByCategoryMatch(regionMap, userCategories);


        
        

        // 5️⃣ Day-wise planning
        List<Map<String, Object>> dailyPlans = new ArrayList<>();

        for (int day = 0; day < Math.min(totalDays, orderedRegions.size()); day++) {
        	
        	// 🔄 Rotate categories so skipped one comes next day
        	Collections.rotate(rotatedCategories, -1);


            String region = orderedRegions.get(day);
            List<Landmark> regionPlaces = regionMap.get(region);

            List<Landmark> selected = new ArrayList<>();

            
            
            if (userCategories.isEmpty()) {
                // No category selected → purely priority-based plan
                regionPlaces.stream()
                        .sorted(Comparator.comparingInt(Landmark::getPriority))
                        .limit(placesPerDay)
                        .forEach(selected::add);
            } else {

       
            // ---------- A. FAIR CATEGORY DISTRIBUTION ----------
            int baseQuota = placesPerDay / userCategories.size();
            int remainder = placesPerDay % userCategories.size();

            for (String userCat : rotatedCategories) {
 

                int quota = baseQuota;
                if (remainder > 0) {
                    quota++;
                    remainder--;
                }

                List<Landmark> matches = regionPlaces.stream()
                        .filter(l -> l.getCategory() != null)
                        .filter(l -> l.getCategory().equalsIgnoreCase(userCat))
                        .sorted(Comparator.comparingInt(Landmark::getPriority))
                        .limit(quota)
                        .collect(Collectors.toList());


                selected.addAll(matches);
            }

            // ---------- B. CONTROLLED FALLBACK ----------
            if (selected.size() < placesPerDay) {

                List<Landmark> fallback = regionPlaces.stream()
                        .filter(l -> !selected.contains(l))
                        .sorted(Comparator.comparingInt(Landmark::getPriority))
                        .collect(Collectors.toList());

                for (Landmark l : fallback) {
                    if (selected.size() >= placesPerDay) break;
                    selected.add(l);
                }
            }
            }

            // ---------- C. ROUTE OPTIMIZATION ----------
            Map<String, Object> routeData = optimizeRouteWithPolyline(selected);

            List<Landmark> optimizedStops =
                    (List<Landmark>) routeData.get("stops");

            String polyline =
                    (String) routeData.get("polyline");
            
            double distanceKm =
                    (double) routeData.get("distanceKm");
            
            List<Map<String, Object>> segments =
                    (List<Map<String, Object>>) routeData.get("segments");

            



         
            Map<String, Object> dayPlan = new HashMap<>();

            dayPlan.put("day", day + 1);
            dayPlan.put("region", region);
            dayPlan.put("stops", convertToMapList(optimizedStops));
            dayPlan.put("polyline", polyline);
            dayPlan.put("segments", segments);
         // 🍽️ Nearby restaurants
            List<Map<String, Object>> restaurants =
                    getNearbyRestaurants(region);

            dayPlan.put("restaurants", restaurants);


            
         // 🏨 HOTEL RECOMMENDATION (OPTIONAL)
            List<Map<String, Object>> hotelsForRegion =
                    (totalDays == 1 || !userSelectedHotel)
                        ? List.of()
                        : hotelService.getHotelsByRegionAndCategory(region, hotelType)
                            .stream()
                            .limit(2)
                            .map(h -> {
                                Map<String, Object> m = new HashMap<>();
                                m.put("name", h.getName());
                                m.put("category", h.getCategory());
                                m.put("pricePerNight", h.getPricePerNight());
                                return m;
                            })
                            .toList();




            // ✅ NOW attach hotels
            dayPlan.put("hotels", hotelsForRegion);

            


            
            
         // 🌦️ WEATHER (use first stop of the day)
            Landmark firstStop = optimizedStops.get(0);

            JSONObject weather = weatherService.getWeather(
                    firstStop.getLatitude(),
                    firstStop.getLongitude()
            );

            String condition = weather
                    .getJSONArray("weather")
                    .getJSONObject(0)
                    .getString("main");   // Sunny / Rain / Clouds

            double temperature = weather
                    .getJSONObject("main")
                    .getDouble("temp");

            Map<String, Object> weatherInfo = new HashMap<>();
            weatherInfo.put("condition", condition);
            weatherInfo.put("temperature", temperature);

            dayPlan.put("weather", weatherInfo);
            double entryFeeTotal =
            	    getEntryFeeTotal(
            	        (List<Map<String, Object>>) dayPlan.get("stops")
            	    );

            	dayPlan.put("entryFeeTotal", entryFeeTotal);


           
            
            
            int foodCostPerPerson =
                    getFoodCostPerPerson(hotelType);

            dayPlan.put("foodCostPerPerson", foodCostPerPerson);
            
            
            
            
            
            double hotelCost = 0;

            if (userSelectedHotel && totalDays > 1) {

                hotelCost =
                    getHotelCost(
                        (List<Map<String, Object>>) dayPlan.get("hotels")
                    ) / 2;   // per person
            }

            dayPlan.put("hotelCost", hotelCost);




            
            double transportCost =
                    getTransportCost(travelMode, distanceKm);

            dayPlan.put("transportCost", transportCost);
            dayPlan.put("distanceKm", distanceKm);
            
            
            double dailyTotal =
            	      foodCostPerPerson
            	    + transportCost
            	    + entryFeeTotal
            	    + hotelCost;

            dayPlan.put("dailyTotal", dailyTotal);





            dailyPlans.add(dayPlan);
            
            
            
            
            
            
            System.out.println("DAY " + (day + 1));
            System.out.println("ROTATED CATEGORIES = " + rotatedCategories);
            System.out.println("FINAL CATEGORIES = " +
                    selected.stream()
                            .map(Landmark::getCategory)
                            .collect(Collectors.toList()));


            // ✅ DEBUG (TEMPORARY)
            System.out.println("DAY " + (day + 1));
            System.out.println("USER CATEGORIES = " + userCategories);
            System.out.println("FINAL CATEGORIES = " +
            		selected.stream()
            .map(Landmark::getCategory)
            .collect(Collectors.toList())
);
        }

           


        
        System.out.println("DAYS GENERATED = " + dailyPlans.size());

        
        double finalTripTotal = 0;

        for (Map<String, Object> day : dailyPlans) {

            Object val = day.get("dailyTotal");

            if (val != null) {
                finalTripTotal += Double.parseDouble(val.toString());
            }
        }


        String narration = generateNarrationWithGroq(dailyPlans);

        return Map.of(
                "itinerary", dailyPlans,
                "narration", narration,
                "finalTripTotal", finalTripTotal
        );


        }
    

    /* =========================
       REGION ORDERING LOGIC
       ========================= */
    private List<String> orderRegionsByDistance(Map<String, List<Landmark>> regionMap) {

        Map<String, double[]> centroids = new HashMap<>();

        for (String region : regionMap.keySet()) {
            List<Landmark> list = regionMap.get(region);

            double avgLat = list.stream().mapToDouble(Landmark::getLatitude).average().orElse(0);
            double avgLng = list.stream().mapToDouble(Landmark::getLongitude).average().orElse(0);

            centroids.put(region, new double[]{avgLat, avgLng});
        }

        List<String> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        String current = centroids.keySet().iterator().next();
        ordered.add(current);
        visited.add(current);

        while (visited.size() < centroids.size()) {

            String next = null;
            double minDist = Double.MAX_VALUE;

            for (String candidate : centroids.keySet()) {
                if (visited.contains(candidate)) continue;

                double dist = haversine(
                        centroids.get(current)[0], centroids.get(current)[1],
                        centroids.get(candidate)[0], centroids.get(candidate)[1]
                );

                if (dist < minDist) {
                    minDist = dist;
                    next = candidate;
                }
            }

            ordered.add(next);
            visited.add(next);
            current = next;
        }

        return ordered;
    }

    /* =========================
       HAVERSINE DISTANCE
       ========================= */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth radius km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return 2 * R * Math.asin(Math.sqrt(a));
    }

    /* =========================
       OSRM ROUTE OPTIMIZATION
       ========================= */
    private Map<String, Object> optimizeRouteWithPolyline(List<Landmark> stops) {
    	
    	System.out.println(">>> optimizeRouteWithPolyline CALLED <<<");
    	System.out.println("Stops size = " + (stops == null ? "null" : stops.size()));


    	if (stops == null || stops.size() < 2) {

    	    System.out.println(">>> SKIPPED OSRM, stops size = " + 
    	        (stops == null ? "null" : stops.size()));

    	    return Map.of(
    	        "stops", stops,
    	        "polyline", null
    	    );
        }

        try {
            // 1️⃣ Build coordinates in DAY ORDER
        	// REMOVE DUPLICATES FIRST
        	// 1️⃣ REMOVE DUPLICATES
        	List<Landmark> uniqueStops = stops.stream()
        	    .collect(Collectors.collectingAndThen(
        	        Collectors.toMap(
        	            l -> l.getLatitude() + "," + l.getLongitude(),
        	            l -> l,
        	            (a, b) -> a
        	        ),
        	        m -> new ArrayList<>(m.values())
        	    ));

        	// 2️⃣ SORT NEAREST FIRST
        	List<Landmark> reorderedStops = sortNearest(uniqueStops);

        	// 3️⃣ BUILD COORDS USING SORTED ORDER
        	String coords = reorderedStops.stream()
        	    .map(l -> l.getLongitude() + "," + l.getLatitude())
        	    .collect(Collectors.joining(";"));


            // ✅ route API (LOCAL OSRM)
            
            
        	String url =
        		    "http://localhost:5000/route/v1/driving/"
        		    + coords
        		    + "?overview=full&geometries=polyline&steps=true";

            System.out.println("OSRM URL = " + url);
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("OSRM RAW RESPONSE = " + response);
            System.out.println("OSRM RAW RESPONSE = " + response);
            JSONObject json = new JSONObject(response);

            // ✅ CORRECT response parsing
            JSONObject route = json
                    .getJSONArray("routes")
                    .getJSONObject(0);

            String polyline = route.getString("geometry");

            double distanceKm =
                    route.getDouble("distance") / 1000;

            // 🔥 ADD THIS
            JSONArray legs = route.getJSONArray("legs");

            List<Map<String, Object>> segments = new ArrayList<>();

            for (int i = 0; i < legs.length(); i++) {

                JSONObject leg = legs.getJSONObject(i);

                double legDistance =
                        leg.getDouble("distance") / 1000; // km

                double legTime =
                        leg.getDouble("duration") / 60; // minutes

                Map<String, Object> m = new HashMap<>();
                m.put("distanceKm", legDistance);
                m.put("timeMin", legTime);

                segments.add(m);
            }


         // ROUTE API DOES NOT REORDER – KEEP SAME ORDER
            
            
            

            Map<String, Object> result = new HashMap<>();
            result.put("stops", reorderedStops);
            result.put("polyline", polyline);
            result.put("distanceKm", distanceKm);

            // 🔥 ADD THIS
            result.put("segments", segments);
            System.out.println("SEGMENTS FROM OSRM = " + segments);


            
            System.out.println(">>> OSRM RESULT RETURNED <<<");
            return result;



        } catch (Exception e) {

            e.printStackTrace(); // <-- ADD THIS

            Map<String, Object> fallback = new HashMap<>();
            fallback.put("stops", stops);
            fallback.put("polyline", null);
            return fallback;
        }

    }
    
    

    


    /* =========================
       RESPONSE MAPPER
       ========================= */
    private List<Map<String, Object>> convertToMapList(List<Landmark> list) {
        return list.stream().map(l -> {
            Map<String, Object> m = new HashMap<>();

            // EXISTING (DO NOT CHANGE)
            m.put("name", l.getName());
            m.put("category", l.getCategory());
            m.put("lat", l.getLatitude());
            m.put("lng", l.getLongitude());
            m.put("region", l.getRegion());

            // ✅ NEW FIELDS FROM DB
            m.put("rating", l.getRating());
            m.put("openingTime", l.getOpeningTime());
            m.put("closingTime", l.getClosingTime());
            m.put("entryFee", l.getEntryFee());

            return m;
        }).collect(Collectors.toList());
    }

    
    
    
    
    
    
    private String generateNarrationWithGroq(List<Map<String, Object>> itinerary) {

        try {
            String prompt = """
            You are given a finalized travel itinerary in JSON.
            Do NOT add, remove, reorder, or modify any place or day.
            Only convert the itinerary into a simple, professional travel narration.
            Keep it concise and friendly.
            JSON:
            """ + itinerary.toString();

            Map<String, Object> request = new HashMap<>();
            request.put("model", "llama3-8b-8192");
            request.put("messages", List.of(
                    Map.of("role", "user", "content", prompt)
            ));
            request.put("temperature", 0.4);

            String GROQ_API_KEY = "YOUR_GROQ_API_KEY";

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + GROQ_API_KEY);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(request, headers);

            String response = restTemplate.postForObject(
                    "https://api.groq.com/openai/v1/chat/completions",
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject(response);
            return json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (Exception e) {
            return ""; // SAFE FALLBACK
        }
    }

    
    public void saveItinerary(String tripName, String itineraryJson) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();

        User user = userRepository.findByUsername(username);

        Itinerary itinerary = new Itinerary();
        itinerary.setUser(user);
        itinerary.setTripName(tripName);
        itinerary.setGeneratedPlan(itineraryJson);

        itineraryRepository.save(itinerary);
    }

    public List<Itinerary> getTripsForUser(String username) {
        User user = userRepository.findByUsername(username);
        return itineraryRepository.findByUser(user);
    }
    
    
    public void deleteTrip(Long id) {
        itineraryRepository.deleteById(id);
    }

    private List<Landmark> sortNearest(List<Landmark> stops) {

        if (stops.size() <= 2) return stops;

        List<Landmark> sorted = new ArrayList<>();
        List<Landmark> remaining = new ArrayList<>(stops);

        sorted.add(remaining.remove(0));

        while (!remaining.isEmpty()) {

            Landmark last = sorted.get(sorted.size() - 1);

            Landmark nearest = remaining.get(0);
            double minDist = Double.MAX_VALUE;

            for (Landmark l : remaining) {

                double d = haversine(
                    last.getLatitude(), last.getLongitude(),
                    l.getLatitude(), l.getLongitude()
                );

                if (d < minDist) {
                    minDist = d;
                    nearest = l;
                }
            }

            sorted.add(nearest);
            remaining.remove(nearest);
        }

        return sorted;
    }
    
    
    
    private List<Map<String, Object>> getNearbyRestaurants(String region) {

        return landmarkRepository
            .findByRegionAndCategoryIgnoreCase(region, "Food")
            .stream()
            .limit(3)   // ONLY 3 restaurants
            .map(l -> {
                Map<String, Object> m = new HashMap<>();
                m.put("name", l.getName());

                // your DB price stored in entryFee
                double price = 300;

                try {
                    if (l.getEntryFee() != null && !l.getEntryFee().isBlank()) {
                        price = Double.parseDouble(
                            l.getEntryFee().replaceAll("[^0-9]", "")
                        );
                    }
                } catch (Exception e) {
                    price = 300;
                }


                m.put("price", price);
                return m;
            })
            .toList();
    }



    


    
}
