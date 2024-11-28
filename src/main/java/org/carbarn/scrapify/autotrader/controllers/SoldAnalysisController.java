package org.carbarn.scrapify.autotrader.controllers;

import org.carbarn.scrapify.autotrader.dto.GroupWiseVehicleCount;
import org.carbarn.scrapify.autotrader.services.SoldAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sold-analysis")
public class SoldAnalysisController {

    private final SoldAnalysisService soldAnalysisService;

    @Autowired
    public SoldAnalysisController(SoldAnalysisService soldAnalysisService) {
        this.soldAnalysisService = soldAnalysisService;
    }

    @GetMapping("/sold-count")
    public ResponseEntity<?> getSalesSummary(
            @RequestParam String groupBy,
            @RequestParam(required = false) String vinPrefix,
            @RequestParam String soldDateStart,
            @RequestParam String soldDateEnd) {

        if (!isValidGroupByParameter(groupBy)) {
            String errorMessage = "Invalid 'groupBy' parameter. Allowed values are: make, model, manufactureYear.";
            return ResponseEntity.badRequest().body(Map.of("error", errorMessage)); // Return proper error message
        }

        List<GroupWiseVehicleCount> summary = soldAnalysisService.getSalesSummary(groupBy, soldDateStart, soldDateEnd,vinPrefix);

        if (summary.isEmpty()) {
            String noDataMessage = "No sales data found for the specified date range and grouping.";
            return ResponseEntity.ok(Map.of("message", noDataMessage));
        }

        return ResponseEntity.ok(summary);
    }

    private boolean isValidGroupByParameter(String groupBy) {
        List<String> validFields = List.of("make", "model", "manufactureYear");
        return validFields.containsAll(List.of(groupBy.split(",")));
    }
}
