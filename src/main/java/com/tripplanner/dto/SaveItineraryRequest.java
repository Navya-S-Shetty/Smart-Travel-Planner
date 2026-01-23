package com.tripplanner.dto;

public class SaveItineraryRequest {

	private String tripName;
	private String itineraryJson;

	public String getTripName() {
		return tripName;
	}

	public void setTripName(String tripName) {
		this.tripName = tripName;
	}

	public String getItineraryJson() {
		return itineraryJson;
	}

	public void setItineraryJson(String itineraryJson) {
		this.itineraryJson = itineraryJson;
	}
}
