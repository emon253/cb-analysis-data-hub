package org.carbarn.scrapify.autotrader.controllers;

import com.google.gson.Gson;
import org.carbarn.scrapify.autotrader.domain.AutotraderDealer;
import org.carbarn.scrapify.autotrader.repositories.AutotraderDealerRepository;
import org.carbarn.scrapify.autotrader.services.AutotraderScraperService;
import org.carbarn.scrapify.autotrader.services.AutotraderPersistenceService;
import org.carbarn.scrapify.autotrader.services.ScraperStatusService;
import org.carbarn.scrapify.autotrader.services.VehicleSoldUpdateService;
import org.carbarn.scrapify.consts.ConstData;
import org.carbarn.scrapify.exceptions.ScrapifyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scrapper")
public class ScrapperController {

    private final AutotraderScraperService scraperService;

    private final ScraperStatusService statusService;
    private final VehicleSoldUpdateService soldUpdateService;private Thread scraperThread;

    @Autowired
    public ScrapperController(AutotraderScraperService scraperService, ScraperStatusService statusService, VehicleSoldUpdateService soldUpdateService) {
        this.scraperService = scraperService;
        this.statusService = statusService;
        this.soldUpdateService = soldUpdateService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("<h1>11th Deploy with success!</h1>" +
                "<br/> Hi Yasin Chowdhury! <br/>You have successfully created CI/CD pipeline with you spring boot application <br/>" +
                " <h2>----------------ALL THE BEST----------------</h2>");
    }

    @GetMapping("/dealerWise/start")
    public ResponseEntity<String> startDealerWiseScraper() {

        if (scraperThread == null || !scraperThread.isAlive()) {

            scraperThread = new Thread(() -> {
                statusService.switchScraperStatus("RUNNING");
                try {
                    scraperService.startScraperForDearWiseData();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                statusService.switchScraperStatus("STOPPED");

            });
            scraperThread.start();
            return ResponseEntity.ok().body("Scraper started ");
        } else {
            return ResponseEntity.ok("Scraper is already running.");
        }
    }


    @GetMapping("/dealer-individual/start")
    public ResponseEntity<?> startIndividualDealerScraper(@RequestParam(required = false) String dealers) {

        if (dealers == null || dealers.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Dealers parameter is required and cannot be empty.");
        }

        try {
            List<Long> dealerIds = Arrays.stream(dealers.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            // Run the scraping process asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    scraperService.startScraperForDearWiseData(dealerIds);
//                    soldUpdateService.updateDealerWiseVehicleSoldStatus(dealerIds);
                } catch (Exception e) {
                    // Log the exception
                    System.out.println(e);
                }
            });

            return ResponseEntity.ok().body("Scraping started.");
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Dealers parameter must contain a comma-separated list of numbers.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during request processing.");
        }
    }

    @GetMapping("/allPage/start")
    public ResponseEntity<String> startAllPageScraper() {
        if (scraperThread == null || !scraperThread.isAlive()) {
            scraperThread = new Thread(() -> {
                statusService.switchScraperStatus("RUNNING");
                scraperService.startScraperPageWiseData();
                statusService.switchScraperStatus("STOPPED");
            });
            scraperThread.start();
            return ResponseEntity.ok("Scraper started.");
        } else {
            return ResponseEntity.ok("Scraper is already running.");
        }

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
        soldUpdateService.updateDealerWiseVehicleSoldStatus(ConstData.getDealers());
    }

    @GetMapping("/update-sold-individual")
    public void updateIndividualVehicles(@RequestParam(required = false) String dealers) {
        List<Long> dealerIds = Arrays.stream(dealers.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        soldUpdateService.updateDealerWiseVehicleSoldStatus(dealerIds);
    }

}
