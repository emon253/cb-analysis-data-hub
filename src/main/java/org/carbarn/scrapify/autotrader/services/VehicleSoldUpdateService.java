package org.carbarn.scrapify.autotrader.services;

import lombok.extern.log4j.Log4j2;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderApiResponse;
import org.carbarn.scrapify.autotrader.repositories.AutotraderVehicleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
public class VehicleSoldUpdateService {

    private final AutotraderVehicleRepository vehicleRepository;
    private final RestTemplate restTemplate;
    private final DataProcessor dataProcessor;

    @Value("${autotrader.individual_product_api_url}")
    private String individualProductAPIUrl;
    @Value("${scraping.interval.autotrader}")
    private Long interval;

    public VehicleSoldUpdateService(AutotraderVehicleRepository vehicleRepository, RestTemplate restTemplate, DataProcessor dataProcessor) {
        this.vehicleRepository = vehicleRepository;
        this.restTemplate = restTemplate;
        this.dataProcessor = dataProcessor;
    }

    public void updateVehicleSoldStatus() {
        log.info("Starting updateVehicleSoldStatus");
        List<AutotraderCarListing> vehicles = vehicleRepository.findAll();
        log.info("Fetched {} vehicles from repository", vehicles.size());
        int chunkSize = 100; // Adjust chunk size as needed
        List<List<AutotraderCarListing>> chunks = chunkList(vehicles, chunkSize);

        int totalVehicles = vehicles.size();
        AtomicInteger completedVehicles = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(5); // Adjust thread pool size as needed

        for (List<AutotraderCarListing> chunk : chunks) {
            executor.submit(() -> {
                try {
                    updateVehicles(chunk);
                    synchronized (this) {
                        completedVehicles.addAndGet(chunk.size());
                        log.info("Completed processing {} of {} vehicles", completedVehicles, totalVehicles);
                    }
                } catch (Exception e) {
                    log.error("Error updating vehicles chunk: {}", e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            if (executor.awaitTermination(1, TimeUnit.HOURS)) {
                log.info("Completed all tasks within the timeout");
            } else {
                log.warn("Timeout occurred before completing all tasks");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Executor interrupted: {}", e.getMessage());
        }
    }


    public void updateDealerWiseVehicleSoldStatus(List<Long> dealers) {
        List<AutotraderCarListing> vehicles = new ArrayList<>();

        List<Boolean> collect = dealers.stream()
                .map(vehicleRepository::getAllVehicleByAutoTraderDealerId)
                .map(vehicles::addAll)
                .toList();
        log.info("{} vehicles found to process for update", vehicles.size());
        if (!collect.isEmpty()) {
            updateVehicles(vehicles);
        }
    }

    private void updateVehicles(List<AutotraderCarListing> vehicles) {
        int total = vehicles.size();

        for (int i = 0; i < vehicles.size(); i++) {
            AutotraderCarListing vehicle = vehicles.get(i);
            Long autoTraderId = vehicle.getAutoTraderId();
            String apiUrl = individualProductAPIUrl + autoTraderId;

            try {

                ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
                AutotraderApiResponse autotraderApiResponse = dataProcessor.processResponseForObject(response.getBody());

                if (response.getStatusCode() == HttpStatus.BAD_REQUEST && response.getBody() != null
                        && response.getBody().contains("Listing not found")) {
                    log.info("Vehicle {} marking as sold", autoTraderId);
                    markVehicleAsSold(vehicle);
                } else {
                    updateVehicleDeletedAt(vehicle, autotraderApiResponse);
                }
            } catch (HttpClientErrorException.NotFound e) {
                log.info("Vehicle {} not found, marking as sold", autoTraderId);
                markVehicleAsSold(vehicle);
            } catch (HttpClientErrorException e) {
                log.error("HTTP Error while updating sold status for vehicle {}: {}", autoTraderId, e.getMessage());
                if (e.getMessage().contains("Listing not found")) {
                    markVehicleAsSold(vehicle);
                }
            } catch (Exception e) {
                log.error("Error while updating sold status for vehicle {}: {}", autoTraderId, e.getMessage());
            }

            // Delay between requests
            if (i < vehicles.size() - 1) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Thread interrupted: {}", e.getMessage());
                }
            }

        }

        log.info("Completed updating vehicles");
    }


    private void markVehicleAsSold(AutotraderCarListing vehicle) {
        log.info("Listing not found and updating vehicle {} as sold", vehicle.getAutoTraderId());
        vehicle.setStatus("SOLD");
        vehicle.setSoldDate(LocalDate.now().toString());
        vehicleRepository.save(vehicle);
    }

    private void updateVehicleDeletedAt(AutotraderCarListing vehicle, AutotraderApiResponse autotraderApiResponse) {
        Optional.ofNullable(autotraderApiResponse)
                .map(AutotraderApiResponse::getDeleted_at)
                .ifPresentOrElse(deletedAt -> {
                    if (vehicle.getStatus().equals("SOLD")) {
                        log.info("Updating Vehicle {} marking as sold at {}", autotraderApiResponse.getId(), deletedAt);
                    } else {
                        log.info("New Vehicle {} found for sold at {}", autotraderApiResponse.getId(), deletedAt);
                    }
                    vehicle.setDeletedAt(deletedAt);
                    vehicle.setStatus("SOLD");
                    vehicle.setSoldDate(deletedAt);
                    vehicleRepository.save(vehicle);
                }, () -> {
                    log.info("Vehicle {} found in live site", autotraderApiResponse.getId());
                });
    }

    private <T> List<List<T>> chunkList(List<T> list, int chunkSize) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(i, Math.min(list.size(), i + chunkSize)));
        }
        return chunks;
    }
}
