package org.carbarn.scrapify.autotrader.repositories;

import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarPricingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutotraderPriceHistoryRepository extends JpaRepository<AutotraderCarPricingHistory,Integer> {
    Optional<AutotraderCarPricingHistory> findByAutotraderCarListing(AutotraderCarListing vehicle);
}
