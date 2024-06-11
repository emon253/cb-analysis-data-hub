package org.carbarn.scrapify.autotrader.services;


import org.carbarn.scrapify.autotrader.dto.response.DealersSummary;
import org.carbarn.scrapify.autotrader.repositories.AutotraderVehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutotraderDataSummaryService {

    private final AutotraderVehicleRepository vehicleRepository;

    public AutotraderDataSummaryService(AutotraderVehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }
    public Page<DealersSummary> getDealerSalesSummary(String startDate, String endDate, Long dealerId,String vin, Pageable pageable) {
        return vehicleRepository.getDealerSalesSummary(startDate, endDate, dealerId,vin, pageable);
    }

}
