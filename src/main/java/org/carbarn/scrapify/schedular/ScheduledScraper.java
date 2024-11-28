package org.carbarn.scrapify.schedular;

import lombok.extern.log4j.Log4j2;
import org.carbarn.scrapify.autotrader.domain.Flag;
import org.carbarn.scrapify.autotrader.dto.AutotraderScrapperStatus;
import org.carbarn.scrapify.autotrader.repositories.FlagRepository;
import org.carbarn.scrapify.autotrader.services.AutotraderScraperService;
import org.carbarn.scrapify.autotrader.services.VehicleSoldUpdateService;
import org.carbarn.scrapify.consts.ConstData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Log4j2
@Component
@EnableScheduling
@Configuration
public class ScheduledScraper {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        taskScheduler.setThreadNamePrefix("scheduled-task-");
        taskScheduler.initialize();
        return taskScheduler;
    }
    private final AutotraderScraperService scraperService;
    @Autowired
    private VehicleSoldUpdateService soldUpdateService;

    @Autowired
    private FlagRepository flagRepository;

    public ScheduledScraper(AutotraderScraperService scraperService) {
        this.scraperService = scraperService;
    }

//    @Scheduled(cron = "0 0 0,6,12,18 * * ?", zone = "Asia/Dhaka")
//    @Scheduled(cron = "0 0 * * *", zone = "Australia/Sydney")
    @Scheduled(cron = "0 0 0,12 * * ?", zone = "Australia/Sydney")
    public void scrapeDataDealerWise() throws InterruptedException {
        logTheSchedulerExecutionTime("Scraping started...");
        scraperService.startScraperForDearWiseData();
        logTheSchedulerExecutionTime("Scraping ended...");
//    soldUpdateService.updateDealerWiseVehicleSoldStatus(dealers);
    }


    private static void logTheSchedulerExecutionTime(String message) {
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String formattedDate = sdf.format(currentDate);

        log.info(message + formattedDate);
    }

}