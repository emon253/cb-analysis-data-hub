package org.carbarn.scrapify.autotrader.services;

import lombok.extern.log4j.Log4j2;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderApiResponse;
import org.carbarn.scrapify.autotrader.repositories.AutotraderVehicleRepository;
import org.carbarn.scrapify.consts.ConstData;
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
import java.util.stream.Collectors;

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
        List<AutotraderCarListing> vehicles = vehicleRepository.findAll();
        updateVehicles(vehicles);
    }

    public void updateDealerWiseVehicleSoldStatus() {
        List<AutotraderCarListing> vehicles = new ArrayList<>();

        List<Boolean> collect = ConstData.getDealers().stream()
                .map(vehicleRepository::getAllVehicleByAutoTraderDealerId)
                .map(vehicles::addAll)
                .toList();
        log.info("{} vehicles found to process for update", vehicles.size());
        if (!collect.isEmpty()) {
            updateVehicles(vehicles);
        }
    }

    private void updateVehicles(List<AutotraderCarListing> vehicles) {
        int count = vehicles.size();
        for (AutotraderCarListing vehicle : vehicles) {
            Long autoTraderId = vehicle.getAutoTraderId();
            String apiUrl = individualProductAPIUrl + autoTraderId;

            try {
                log.info("Request for url {}", apiUrl);
//                Thread.sleep(interval);

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
            } catch (Exception e) {
                log.error("Error while updating sold status for vehicle {}: {}", autoTraderId, e.getMessage());
            }
            count-=1;
            log.info("Remaining {} of {} ",count, vehicles.size());
        }
    }

    private void markVehicleAsSold(AutotraderCarListing vehicle) {
        vehicle.setStatus("SOLD");
        vehicle.setSoldDate(LocalDate.now().toString());
        vehicleRepository.save(vehicle);
    }

    private void updateVehicleDeletedAt(AutotraderCarListing vehicle, AutotraderApiResponse autotraderApiResponse) {
        Optional.ofNullable(autotraderApiResponse)
                .map(AutotraderApiResponse::getDeleted_at)
                .ifPresentOrElse(deletedAt -> {
                    assert autotraderApiResponse != null;
                    log.info("Vehicle {} marking as sold at {}", autotraderApiResponse.getId(), deletedAt);
                    vehicle.setDeletedAt(deletedAt);
                    vehicle.setStatus("SOLD");
                    vehicle.setSoldDate(deletedAt);
                    vehicleRepository.save(vehicle);
                }, () -> {
                    assert autotraderApiResponse != null;
                    log.info("Vehicle {} found in live site", autotraderApiResponse.getId());
                });
    }

}
