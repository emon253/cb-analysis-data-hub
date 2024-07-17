package org.carbarn.scrapify.carvx.controllers;

import org.carbarn.scrapify.carvx.services.CarvxScrapingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/scrap")
public class CarvxScrapingController {

    @Autowired
    private CarvxScrapingService carvxScrapingService;

    @GetMapping("/carvx")
    public ResponseEntity<String> triggerScraping() {
        CompletableFuture<Void> voidCompletableFuture = carvxScrapingService.performScraping();
        return ResponseEntity.ok("Scraping triggered successfully");
    }
    @GetMapping("/carvx/stop")
    public ResponseEntity<String> stopScraping() {
        carvxScrapingService.stopScraping();
        return ResponseEntity.ok("Scraping process stopped successfully");
    }
}
