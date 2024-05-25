package org.carbarn.scrapify.schedular;

import lombok.extern.log4j.Log4j2;
import org.carbarn.scrapify.autotrader.domain.Flag;
import org.carbarn.scrapify.autotrader.dto.AutotraderScrapperStatus;
import org.carbarn.scrapify.autotrader.repositories.FlagRepository;
import org.carbarn.scrapify.autotrader.services.AutotraderScraperService;
import org.carbarn.scrapify.autotrader.services.VehicleSoldUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@EnableScheduling
public class ScheduledScraper {

    private final AutotraderScraperService scraperService;
    @Autowired
    private VehicleSoldUpdateService soldUpdateService;

    @Autowired
    private FlagRepository flagRepository;

    public ScheduledScraper(AutotraderScraperService scraperService) {
        this.scraperService = scraperService;
    }

    public void scrapeUpdateStatus() {
        log.info("Updating status started...");
        soldUpdateService.updateDealerWiseVehicleSoldStatus();
        log.info("Updating status ended...");

    }

    //    @Scheduled(cron = "0 * * * * *")
//    @Scheduled(fixedDelay = 3000)
    @Scheduled(cron = "0 0 0 * * ?", zone = "Australia/Sydney")
    public void scrapeDataDealerWise() throws InterruptedException {
        log.info("Scraping started...");
        if (shouldRun(AutotraderScrapperStatus.AUTOTRADER_SCRAPPER_STATUS_GROUP_WISE.name())) {
            scraperService.startScraperForDearWiseData();
        }
        soldUpdateService.updateDealerWiseVehicleSoldStatus();
        log.info("Completed Scraping and sold status update...");
    }

//    Boolean shouldRun(String name) {
//        return flagRepository.findByName(name)
//                .map(Flag::getValue)
//                .map(status -> status.equals("RUNNING"))
//                .orElse(false);
//    }
    Boolean shouldRun(String name) {
        return Boolean.TRUE;
    }


}