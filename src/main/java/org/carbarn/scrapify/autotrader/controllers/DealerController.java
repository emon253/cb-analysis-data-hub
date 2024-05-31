package org.carbarn.scrapify.autotrader.controllers;

import org.carbarn.scrapify.autotrader.domain.AutotraderDealer;
import org.carbarn.scrapify.autotrader.services.DealerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
