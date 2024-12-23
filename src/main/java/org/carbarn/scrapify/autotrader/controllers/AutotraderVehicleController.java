package org.carbarn.scrapify.autotrader.controllers;

import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.carbarn.scrapify.autotrader.dto.ProductFilterCriteria;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderDataAndSummary;
import org.carbarn.scrapify.autotrader.services.AutotraderVehicleService;
import org.carbarn.scrapify.autotrader.services.export.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@CrossOrigin(originPatterns = "*",allowCredentials = "false")
@RestController
@RequestMapping("/vehicles")
public class AutotraderVehicleController {

    private final AutotraderVehicleService vehicleService;

    @Autowired
    public AutotraderVehicleController(AutotraderVehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public ResponseEntity<Page<AutotraderCarListing>> getAllVehicles(@PageableDefault(15) Pageable pageable) {
        Page<AutotraderCarListing> page = vehicleService.getAllVehicle(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/dealer/{dealerId}")
    public ResponseEntity<Page<AutotraderCarListing>> getAllVehicleByAutotraderDealerId(@PathVariable Long dealerId, @PageableDefault(size = 15) Pageable pageable) {
        Page<AutotraderCarListing> page = vehicleService.getAllVehicleByAutotraderDealerId(dealerId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AutotraderCarListing> getVehicleById(@PathVariable Long id) {
        AutotraderCarListing carListing = vehicleService.getById(id);
        return ResponseEntity.ok(carListing);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<AutotraderCarListing>> getVehiclesByStatus(
            @PathVariable String status, Pageable pageable) {
        Page<AutotraderCarListing> vehicles = vehicleService.getVehiclesByStatus(status, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/vin-starts-with/{prefix}")
    public ResponseEntity<Page<AutotraderCarListing>> getVehiclesByVinStartsWith(
            @PathVariable String prefix, Pageable pageable) {
        Page<AutotraderCarListing> vehicles = vehicleService.getVehiclesByVinStartsWith(prefix, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/sold-date-range")
    public ResponseEntity<Page<AutotraderCarListing>> getVehiclesBySoldDateRange(
            @RequestParam String startDate, @RequestParam String endDate,@PageableDefault(size = 15) Pageable pageable) {
        Page<AutotraderCarListing> vehicles = vehicleService.getVehiclesBySoldDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/filterBy")
    public ResponseEntity<AutotraderDataAndSummary> getVehiclesFilterBy(ProductFilterCriteria criteria, @PageableDefault(size = 15) Pageable pageable ){
        AutotraderDataAndSummary vehicles = vehicleService.getFilteredVehiclesWithSummary(criteria, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @Autowired
    private ExportService exportService;

    @GetMapping("/export/csv")
    public ResponseEntity<FileSystemResource> exportDataToCSV() {
        String filePath = "car_listings.csv";
        try {
            exportService.exportDataToCSV(filePath);

            File file = new File(filePath);
            FileSystemResource fileSystemResource = new FileSystemResource(file);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=car_listings.csv");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");

            return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
