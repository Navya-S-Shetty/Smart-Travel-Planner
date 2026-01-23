package com.tripplanner.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String region;
    private String category; // Budget / Mid-range / Premium

    @Column(name = "price_per_night")
    private Integer pricePerNight;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(Integer pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
}
