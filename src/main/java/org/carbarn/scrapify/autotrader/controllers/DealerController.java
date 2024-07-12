package org.carbarn.scrapify.autotrader.controllers;

import org.carbarn.scrapify.autotrader.domain.AutotraderDealer;
import org.carbarn.scrapify.autotrader.services.DealerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scrapper/dealer")
public class DealerController {
    private final DealerService dealerService;

    public DealerController(DealerService dealerService) {
        this.dealerService = dealerService;
    }

    @GetMapping("/autotrader")
    public List<AutotraderDealer> getAllDealersInformaton() {
        return dealerService.getAllDealersInformaton();
    }

    @PatchMapping("/update")
    public ResponseEntity<AutotraderDealer> updateDealer(
            @RequestBody AutotraderDealer dealer) {
        AutotraderDealer updatedDealer = dealerService.updateDealer(dealer);
        return ResponseEntity.ok(updatedDealer);
    }
}
