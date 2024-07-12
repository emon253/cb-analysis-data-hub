package org.carbarn.scrapify.autotrader.repositories;

import org.carbarn.scrapify.autotrader.domain.AutotraderDealer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutotraderDealerRepository extends JpaRepository<AutotraderDealer, Integer> {
    Optional<AutotraderDealer> findByTradingNameAndPhoneLead(String tradingName, String phoneLead);
    Optional<AutotraderDealer> findByAutoTraderDealerId( Long autoTraderDealerId);
}
