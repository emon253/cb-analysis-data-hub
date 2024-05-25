package org.carbarn.scrapify.autotrader.services;

import org.carbarn.scrapify.autotrader.domain.Flag;
import org.carbarn.scrapify.autotrader.repositories.FlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AutotraderScrapperSettingService {
    @Autowired
    private FlagRepository flagRepository;

    public String switchScrapper() throws InterruptedException {
        // Retrieve the current status using Optional
        Optional<Flag> optionalStatusFlag = flagRepository.findByName("AUTOTRADER_SCRAPPER_STATUS");

        if (optionalStatusFlag.isEmpty()) {
            return "Error: Status flag not found.";
        }

        Flag statusFlag = optionalStatusFlag.get();

        // Check current status and toggle it
        String currentStatus = statusFlag.getValue();
        switch (currentStatus) {
            case "RUNNING":
                statusFlag.setValue("STOPPED");
                break;
            case "STOPPED":
                statusFlag.setValue("RUNNING");
                break;
            default:
                return "Error: Invalid status value.";
        }

        // Save the updated status
        flagRepository.save(statusFlag);

        // Return a message indicating the new state
        return "Scrapper status updated to: " + statusFlag.getValue();
    }

    public Boolean isScrapperInRunningState() {
        return flagRepository.findByName("AUTOTRADER_SCRAPPER_STATUS")
                .map(status -> "RUNNING".equals(status.getValue()))
                .orElse(false);
    }

}
