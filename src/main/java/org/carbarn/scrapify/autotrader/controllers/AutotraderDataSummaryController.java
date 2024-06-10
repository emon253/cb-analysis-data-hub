package org.carbarn.scrapify.autotrader.controllers;


import org.carbarn.scrapify.autotrader.dto.response.DealersSummary;
import org.carbarn.scrapify.autotrader.services.AutotraderDataSummaryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-summary")
public class AutotraderDataSummaryController {
    private final AutotraderDataSummaryService vehicleService;

    public AutotraderDataSummaryController(AutotraderDataSummaryService listingService) {
        this.vehicleService = listingService;
    }

    @GetMapping("/dealers")
    public Page<DealersSummary> getDealerSalesSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long dealerId,
            Pageable pageable) {
        return vehicleService.getDealerSalesSummary(startDate, endDate, dealerId, pageable);
    }


}
