package com.tripplanner.dto;

public class ItineraryResponse {

    private Long id;
    private String tripName;
    private String generatedPlan;

    public ItineraryResponse(Long id, String tripName, String generatedPlan) {
        this.id = id;
        this.tripName = tripName;
        this.generatedPlan = generatedPlan;
    }

    public Long getId() {
        return id;
    }

    public String getTripName() {
        return tripName;
    }

    public String getGeneratedPlan() {
        return generatedPlan;
    }
}
