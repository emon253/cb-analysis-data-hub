package org.carbarn.scrapify.autotrader.services.export;

import com.opencsv.CSVWriter;
import lombok.extern.log4j.Log4j2;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarPricingHistory;
import org.carbarn.scrapify.autotrader.repositories.AutotraderVehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


@Log4j2
@Service
public class ExportService {

    @Autowired
    private AutotraderVehicleRepository carListingRepository;

    @Transactional
    public void exportDataToCSV(String filePath) throws IOException {
        List<AutotraderCarListing> carListings = carListingRepository.findAll();

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {
                    "ID", "AutoTraderID", "AutoTraderDealerID", "Source", "SourceID", "VIN", "StockNo",
                    "ManufactureYear", "ColourBody", "Odometer", "Rego", "RegoExpiry", "Make", "Model",
                    "Variant", "Series", "Status", "SoldDate", "URL", "Lat", "Lon", "CreatedAt",
                    "UpdatedAt", "DeletedAt","Created", "Updated", "DealerName", "MinPrice"
            };
            writer.writeNext(header);

            // Write data rows
            for (AutotraderCarListing carListing : carListings) {
                String dealerName = carListing.getDealer().getTradingName();
                int minPrice = carListing.getPricingHistory()
                        .stream()
                        .mapToInt(AutotraderCarPricingHistory::getAdvertisedPrice)
                        .min()
                        .orElse(0);  // Assuming 0 if no prices are available

                String[] row = {
                        carListing.getId().toString(),
                        carListing.getAutoTraderId() != null ? carListing.getAutoTraderId().toString() : "",
                        carListing.getAutoTraderDealerId() != null ? carListing.getAutoTraderDealerId().toString() : "",
                        carListing.getSource(),
                        carListing.getSourceId() != null ? carListing.getSourceId().toString() : "",
                        carListing.getVin(),
                        carListing.getStockNo(),
                        String.valueOf(carListing.getManufactureYear()),
                        carListing.getColourBody(),
                        carListing.getOdometer() != null ? carListing.getOdometer().toString() : "",
                        carListing.getRego(),
                        carListing.getRegoExpiry(),
                        carListing.getMake(),
                        carListing.getModel(),
                        carListing.getVariant(),
                        carListing.getSeries(),
                        carListing.getStatus(),
                        carListing.getSoldDate(),
                        carListing.getUrl(),
                        carListing.getLat(),
                        carListing.getLon(),
                        carListing.getCreatedAt(),
                        carListing.getUpdatedAt(),
                        carListing.getDeletedAt(),
                        String.valueOf(carListing.getCreated()),
                        String.valueOf(carListing.getUpdated()),
                        dealerName,
                        String.valueOf(minPrice)
                };

                writer.writeNext(row);
                log.info("Processed car listing with ID: {}, Dealer: {}, Min Price: {}", carListing.getId(), dealerName, minPrice);
            }
        }
    }
}
