package com.example.controller;

import com.example.dto.GeoResponse;
import com.example.service.LocationService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping(value = "/api/location", produces = MediaType.APPLICATION_JSON_VALUE)
    public GeoResponse getLocation(
            @RequestParam @DecimalMin("73.66") @DecimalMax("135.05") String longitude,
            @RequestParam @DecimalMin("3.86") @DecimalMax("53.55") String latitude) {
        GeoResponse response = locationService.getLocation(longitude, latitude);
        System.out.println("Service返回对象: " + response); // 使用toString()
        return response;
    }
}