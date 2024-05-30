package org.carbarn.scrapify.autotrader.controllers;

import com.google.gson.Gson;
import org.carbarn.scrapify.autotrader.services.AutotraderScraperService;
import org.carbarn.scrapify.autotrader.services.AutotraderPersistenceService;
import org.carbarn.scrapify.autotrader.services.ScraperStatusService;
import org.carbarn.scrapify.autotrader.services.VehicleSoldUpdateService;
import org.carbarn.scrapify.exceptions.ScrapifyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scrapper")
public class ScrapperController {

    private final AutotraderScraperService scraperService;

    private final ScraperStatusService statusService;
    private final VehicleSoldUpdateService soldUpdateService;
    private Thread scraperThread;

    @Autowired
    public ScrapperController(AutotraderScraperService scraperService, ScraperStatusService statusService, VehicleSoldUpdateService soldUpdateService) {
        this.scraperService = scraperService;
        this.statusService = statusService;
        this.soldUpdateService=soldUpdateService;
    }
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("<h1>5th Deploy with success!</h1>" +
                "<br/> Hi Yasin Chowdhury! <br/>You have successfully created CI/CD pipeline with you spring boot application <br/>" +
                " <h2>----------------ALL THE BEST----------------</h2>");
    }

    @GetMapping("/dealerWise/start")
    public ResponseEntity<String> startDealerWiseScraper()  {
        if (scraperThread == null || !scraperThread.isAlive()) {
            scraperThread = new Thread(() -> {
                try {
                    statusService.switchScraperStatus("RUNNING");
                    scraperService.startScraperForDearWiseData();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            scraperThread.start();
            return ResponseEntity.ok("Scraper started.");
        } else {
            return ResponseEntity.ok("Scraper is already running.");
        }
    }



    @GetMapping("/allPage/start")
    public ResponseEntity<String> startAllPageScraper() throws InterruptedException {
        statusService.switchScraperStatus("RUNNING");
        scraperService.startScraperPageWiseData();
        return ResponseEntity.ok("Scraper has been started.");
    }


    @GetMapping("/stop")
    public ResponseEntity<String> stopScraper() {
        statusService.switchScraperStatus("STOPPED");
        if (scraperThread != null && scraperThread.isAlive()) {
            scraperThread.interrupt();
            return ResponseEntity.ok("Scraper stop requested.");
        } else {
            return ResponseEntity.ok("Scraper is not running.");
        }
    }

    @GetMapping("/status/{scraperName}")
    public ResponseEntity<String> checkScraperStatus(@PathVariable String scraperName) {
        try {
            String status = statusService.getScraperStatus(scraperName);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to check status of " + scraperName + ": " + e.getMessage());
        }
    }

    @GetMapping("/check-sold")
    public Boolean checkIfSold(@RequestParam String url) {
        return statusService.fetchIsSoldStatus(url);
    }

    @GetMapping("/update-sold")
    public void updateVehicles() {
        soldUpdateService.updateVehicleSoldStatus();
    }
    @GetMapping("/update-sold-dealerWise")
    public void updateDealerWiseVehicles() {
        soldUpdateService.updateDealerWiseVehicleSoldStatus();
    }
}
