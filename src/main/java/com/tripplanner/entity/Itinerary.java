package com.tripplanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "itineraries")
@Data
@JsonIgnoreProperties({
    "user",
    "hibernateLazyInitializer",
    "handler"
})


public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String tripName;
    private String destinationCity;
    
  
    @Column(columnDefinition = "TEXT")
    private String generatedPlan;

   
    @Column(columnDefinition = "TEXT")
    private String polylinePath;

    private String travelMode; 
    private String travelPace; 
    private String travelCompanion; 
    
    private LocalDate startDate;
    private LocalDate endDate;

    private Double totalEstimatedCost;
    private String weatherAlert;
    
    
    public void setUser(User user) {
        this.user = user;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public void setGeneratedPlan(String generatedPlan) {
        this.generatedPlan = generatedPlan;
    }
    
    public String getGeneratedPlan() {
        return generatedPlan;
    }
    
    public Long getId() {
        return id;
    }

    public String getTripName() {
        return tripName;
    }



}
