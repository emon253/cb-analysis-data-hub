package org.carbarn.scrapify.autotrader.services;

import org.carbarn.scrapify.autotrader.domain.AutotraderDealer;
import org.carbarn.scrapify.autotrader.repositories.AutotraderDealerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DealerService {
    private final AutotraderDealerRepository dealerRepository;

    public DealerService(AutotraderDealerRepository dealerRepository) {
        this.dealerRepository = dealerRepository;
    }

    public List<AutotraderDealer> getAllDealersInformaton(){
     return dealerRepository.findAll();
    }

    public AutotraderDealer updateDealer(AutotraderDealer updatedDealer) {

        Optional<AutotraderDealer> optionalDealer = dealerRepository.findByAutoTraderDealerId(updatedDealer.getAutoTraderDealerId());
        if (optionalDealer.isPresent()) {
            AutotraderDealer dealer = optionalDealer.get();
            dealer.setEnableDataScraping(updatedDealer.getEnableDataScraping());
            return dealerRepository.save(dealer);
        } else {
            throw new IllegalArgumentException("Dealer not found with id: " + updatedDealer.getId());
        }
    }
}
