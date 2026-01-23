package com.tripplanner.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tourist_places")
public class TouristPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tp_id")
    private Integer tpId;

    private String city;

    @Column(name = "place_name")
    private String placeName;

    private String preference;
    private Double rating;

    @Column(name = "is_top")
    private Boolean isTop;

    private Integer visit_time_minutes;
    private Integer travel_time_minutes;
    private Double distance_from_city_km;
    private Integer zone_id;

    private Double latitude;
    private Double longitude;

    private String common_name;
    private String best_time;
    private Integer priority;
    private Integer travel_time;

    // ---- GETTERS & SETTERS ----

    public Integer getTpId() {
        return tpId;
    }

    public void setTpId(Integer tpId) {
        this.tpId = tpId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Boolean getIsTop() {
        return isTop;
    }

    public void setIsTop(Boolean isTop) {
        this.isTop = isTop;
    }

    public Integer getVisit_time_minutes() {
        return visit_time_minutes;
    }

    public void setVisit_time_minutes(Integer visit_time_minutes) {
        this.visit_time_minutes = visit_time_minutes;
    }

    public Integer getTravel_time_minutes() {
        return travel_time_minutes;
    }

    public void setTravel_time_minutes(Integer travel_time_minutes) {
        this.travel_time_minutes = travel_time_minutes;
    }

    public Double getDistance_from_city_km() {
        return distance_from_city_km;
    }

    public void setDistance_from_city_km(Double distance_from_city_km) {
        this.distance_from_city_km = distance_from_city_km;
    }

    public Integer getZone_id() {
        return zone_id;
    }

    public void setZone_id(Integer zone_id) {
        this.zone_id = zone_id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCommon_name() {
        return common_name;
    }

    public void setCommon_name(String common_name) {
        this.common_name = common_name;
    }

    public String getBest_time() {
        return best_time;
    }

    public void setBest_time(String best_time) {
        this.best_time = best_time;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getTravel_time() {
        return travel_time;
    }

    public void setTravel_time(Integer travel_time) {
        this.travel_time = travel_time;
    }
}
