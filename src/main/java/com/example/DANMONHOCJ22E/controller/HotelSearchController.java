package com.example.DANMONHOCJ22E.controller;

import com.example.DANMONHOCJ22E.dto.BranchAvailabilityResponse;
import com.example.DANMONHOCJ22E.dto.HotelSearchRequest;
import com.example.DANMONHOCJ22E.service.HotelSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelSearchController {

    private final HotelSearchService hotelSearchService;

    public HotelSearchController(HotelSearchService hotelSearchService) {
        this.hotelSearchService = hotelSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<BranchAvailabilityResponse>> search(@ModelAttribute HotelSearchRequest request) {
        return ResponseEntity.ok(hotelSearchService.search(request));
    }
}
