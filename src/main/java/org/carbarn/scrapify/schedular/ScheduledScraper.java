package org.carbarn.scrapify.schedular;

import lombok.extern.log4j.Log4j2;
import org.carbarn.scrapify.autotrader.domain.Flag;
import org.carbarn.scrapify.autotrader.dto.AutotraderScrapperStatus;
import org.carbarn.scrapify.autotrader.repositories.FlagRepository;
import org.carbarn.scrapify.autotrader.services.AutotraderScraperService;
import org.carbarn.scrapify.autotrader.services.VehicleSoldUpdateService;
import org.carbarn.scrapify.consts.ConstData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Scheduled(cron = "0 0 0,6,12,18 * * ?", zone = "Australia/Sydney")
    public void scrapeDataDealerWise() throws InterruptedException {
        List<Long> dealers = ConstData.getDealers();
        log.info("Updating status started...");
        scraperService.startScraperForDearWiseData(dealers);
        log.info("Updating status ended...");
        soldUpdateService.updateDealerWiseVehicleSoldStatus(dealers);
        log.info("Completed Scraping and sold status update...");
    }


}