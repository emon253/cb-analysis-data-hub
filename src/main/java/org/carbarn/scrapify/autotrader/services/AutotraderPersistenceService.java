package org.carbarn.scrapify.autotrader.services;

import lombok.extern.log4j.Log4j2;
import org.carbarn.scrapify.autotrader.adapters.AutotraderVehicleConverter;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarPricingHistory;
import org.carbarn.scrapify.autotrader.domain.AutotraderDealer;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderApiResponse;
import org.carbarn.scrapify.autotrader.repositories.AutotraderDealerRepository;
import org.carbarn.scrapify.autotrader.repositories.AutotraderVehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class AutotraderPersistenceService {

    private final AutotraderDealerRepository dealerRepository;
    private final AutotraderVehicleRepository vehicleRepository;

    @Autowired
    public AutotraderPersistenceService(AutotraderDealerRepository dealerRepository, AutotraderVehicleRepository vehicleRepository) {
        this.dealerRepository = dealerRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public void persistVehicleInformation(List<AutotraderApiResponse> processedVehicleList) {
        processedVehicleList.stream()
                .map(AutotraderVehicleConverter::convertSrappedDataToEntity)
                .forEach(this::updateOrSaveVehicle);
    }

    //    private void updateOrSaveVehicle(AutotraderCarListing vehicle) {
//        vehicleRepository.findByAutoTraderId(vehicle.getAutoTraderId()).ifPresentOrElse(
//                existingVehicle -> updateExistingVehicle(existingVehicle, vehicle),
//                () -> saveNewVehicle(vehicle)
//        );
//    }
    private void updateOrSaveVehicle(AutotraderCarListing vehicle) {
        List<AutotraderCarListing> existingVehicles = vehicleRepository.findByAutoTraderId(vehicle.getAutoTraderId());
        if (existingVehicles.isEmpty()) {
            saveNewVehicle(vehicle);
        } else {
            for (AutotraderCarListing existingVehicle : existingVehicles) {
                updateExistingVehicle(existingVehicle, vehicle);
            }
        }
    }


    private void updateExistingVehicle(AutotraderCarListing existingVehicle, AutotraderCarListing newVehicle) {
        log.info("Updating existing vehicle information {}", newVehicle);
        Boolean updatesMade = AutotraderVehicleConverter.updateInformation(existingVehicle, newVehicle);
        if (updatesMade) {
            vehicleRepository.save(existingVehicle);
        } else {
            log.info("No changes found. Vehicle information remains unchanged.");
        }
    }

    private void saveNewVehicle(AutotraderCarListing newVehicle) {
        log.info("Saving new vehicle information {}", newVehicle);
        AutotraderDealer dealer = getDealer(newVehicle);

        updatePricingHistory(newVehicle);

        newVehicle.setDealer(dealer);
        vehicleRepository.save(newVehicle);
    }

    private AutotraderDealer getDealer(AutotraderCarListing vehicle) {
        return Optional.ofNullable(vehicle.getDealer())
                .map(dealer -> dealerRepository.findByTradingNameAndPhoneLead(dealer.getTradingName(), dealer.getPhoneLead())
                        .orElseGet(() -> dealerRepository.save(dealer)))
                .orElse(null);
    }

    private void updatePricingHistory(AutotraderCarListing vehicle) {
        List<AutotraderCarPricingHistory> pricingHistory = vehicle.getPricingHistory();
        if (pricingHistory != null && !pricingHistory.isEmpty()) {
            for (AutotraderCarPricingHistory history : pricingHistory) {
                history.setAutotraderCarListing(vehicle);
            }
        }
    }
}
