package org.carbarn.scrapify.autotrader.adapters;

import lombok.extern.log4j.Log4j2;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarPricingHistory;
import org.carbarn.scrapify.autotrader.domain.AutotraderDealer;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderApiResponse;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderCarPricingHistoryResponse;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderDealerResponse;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderLocationResponse;
import org.springframework.stereotype.Component;

import java.util.*;

@Log4j2
@Component
public class AutotraderVehicleConverter {
    public static AutotraderCarListing convertSrappedDataToEntity(AutotraderApiResponse apiResponse) {
        log.info("Converting response to entity for vehicle: {} dealer {}",apiResponse.getId(),apiResponse.getDealer_id());
        AutotraderCarListing carListing = new AutotraderCarListing();

        // Map basic car details
        carListing.setAutoTraderId(apiResponse.getId());
        carListing.setSource(apiResponse.getSource());
        carListing.setSourceId(apiResponse.getSource_id());
        carListing.setVin(apiResponse.getVin());
        carListing.setStockNo(apiResponse.getStock_no());
        carListing.setManufactureYear(apiResponse.getManu_year());
        carListing.setColourBody(apiResponse.getColour_body());
        carListing.setOdometer(apiResponse.getOdometer());
        carListing.setRego(apiResponse.getRego());
        carListing.setRegoExpiry(apiResponse.getRego_expiry());
        carListing.setMake(apiResponse.getMake());
        carListing.setModel(apiResponse.getModel());
        carListing.setVariant(apiResponse.getVariant());
        carListing.setSeries(apiResponse.getSeries());
        carListing.setStatus(apiResponse.getStatus());
        carListing.setUrl(apiResponse.getUrl());
        carListing.setAutoTraderDealerId(apiResponse.getDealer_id());
        Optional.ofNullable(apiResponse.getLocation()).map(AutotraderLocationResponse::getLat).ifPresent(carListing::setLat);
        Optional.ofNullable(apiResponse.getLocation()).map(AutotraderLocationResponse::getLon).ifPresent(carListing::setLon);
        Optional.ofNullable(apiResponse.getDealer()).map(AutotraderVehicleConverter::convertResponseToDealerEntity).ifPresent(carListing::setDealer);
        // Convert pricing history array if present
        Optional.ofNullable(apiResponse.getPricingHistory())
                .ifPresent(pricingHistoryResponses -> {
                    carListing.setPricingHistory(List.of(convertResponseToPriceEntity(pricingHistoryResponses)));
                });
        // Map timestamps
        carListing.setCreatedAt(apiResponse.getCreated_at());
        carListing.setUpdatedAt(apiResponse.getUpdated_at());
        carListing.setDeletedAt(apiResponse.getDeleted_at());


        return carListing;
    }

    public static   Boolean updateInformation(AutotraderCarListing existingVehicle, AutotraderCarListing newVehicle) {
        boolean updatesMade = false;

        // Check if the createdAt field has changed
        if (!Objects.equals(existingVehicle.getCreatedAt(), newVehicle.getCreatedAt())) {
            existingVehicle.setCreatedAt(newVehicle.getCreatedAt());
            updatesMade = true;
        }

        // Check if the updatedAt field has changed
        if (!Objects.equals(existingVehicle.getUpdatedAt(), newVehicle.getUpdatedAt())) {
            existingVehicle.setUpdatedAt(newVehicle.getUpdatedAt());
            updatesMade = true;
        }
        // Check if the updatedAt field has changed
        if (!Objects.equals(existingVehicle.getDeletedAt(), newVehicle.getDeletedAt())) {
            existingVehicle.setUpdatedAt(newVehicle.getDeletedAt());
            updatesMade = true;
        }
        // Check if the updatedAt field has changed
        if (!Objects.equals(existingVehicle.getAutoTraderDealerId(), newVehicle.getAutoTraderDealerId())) {
            existingVehicle.setAutoTraderDealerId(newVehicle.getAutoTraderDealerId());
            updatesMade = true;
        }

        // Update pricing history based on deletedAt date
        List<AutotraderCarPricingHistory> existingPricingHistory = existingVehicle.getPricingHistory();
        List<AutotraderCarPricingHistory> newPricingHistory = newVehicle.getPricingHistory();

        if (existingPricingHistory != null && newPricingHistory != null) {
            for (AutotraderCarPricingHistory newHistory : newPricingHistory) {
                boolean isNewHistory = existingPricingHistory.stream()
                        .noneMatch(history -> Objects.equals(history.getDeletedAt(), newHistory.getDeletedAt()));

                if (isNewHistory) {
                    // Add new price history entry
                    existingPricingHistory.add(newHistory);
                    newHistory.setAutotraderCarListing(existingVehicle);
                    updatesMade = true;
                }
            }
        }
        return updatesMade;
    }


    public static AutotraderDealer convertResponseToDealerEntity(AutotraderDealerResponse response) {
        AutotraderDealer entity = new AutotraderDealer();
        entity.setTradingName(response.getTrading_name());
        entity.setPhoneLead(response.getPhone_lead());
        entity.setCity(response.getCity());
        entity.setState(response.getState());

        return entity;
    }

    public static AutotraderCarPricingHistory convertResponseToPriceEntity(AutotraderCarPricingHistoryResponse response) {
        AutotraderCarPricingHistory entity = new AutotraderCarPricingHistory();
        entity.setAdvertisedPrice(response.getAdvertised_price());
        entity.setEgcPrice(response.getEgc_price());
        entity.setDriveawayPrice(response.getDriveaway_price());
        entity.setIsFixedPrice(response.getIs_fixed_price());
        entity.setDeletedAt(response.getDeleted_at());
        return entity;
    }
}
