package org.carbarn.scrapify.autotrader.services;

import org.carbarn.scrapify.autotrader.domain.AutotraderDealer;
import org.carbarn.scrapify.autotrader.repositories.AutotraderDealerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DealerService {
    private final AutotraderDealerRepository dealerRepository;

    public DealerService(AutotraderDealerRepository dealerRepository) {
        this.dealerRepository = dealerRepository;
    }

    public List<AutotraderDealer> getAllDealersInformaton(){
     return dealerRepository.findAll();
    }
}
