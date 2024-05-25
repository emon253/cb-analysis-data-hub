package org.carbarn.scrapify.autotrader.repositories;

import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.carbarn.scrapify.autotrader.domain.AutotraderDealer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutotraderVehicleRepository extends JpaRepository<AutotraderCarListing,Long> , JpaSpecificationExecutor<AutotraderCarListing> {

//    Optional<AutotraderCarListing> findByAutoTraderId(Long id);
    List<AutotraderCarListing> findByAutoTraderId(Long id);

    Page<AutotraderCarListing> findByDealer(AutotraderDealer dealer, Pageable pageable);
    Page<AutotraderCarListing> getAllVehicleByAutoTraderDealerId(Long autoTraderDealerId, Pageable pageable);
    List<AutotraderCarListing> getAllVehicleByAutoTraderDealerId(Long autoTraderDealerId);
    default Page<AutotraderCarListing> getAllVehicleByAutoTraderDealerId(Long autoTraderDealerId, Specification<AutotraderCarListing> additionalSpec, Pageable pageable) {
        Specification<AutotraderCarListing> dealerSpec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("dealer").get("id"), autoTraderDealerId);
        Specification<AutotraderCarListing> combinedSpec = Specification.where(dealerSpec).and(additionalSpec);
        return findAll(combinedSpec, pageable);
    }

    Page<AutotraderCarListing> findByStatus(String status, Pageable pageable);

    Page<AutotraderCarListing> findByVinStartingWith(String prefix, Pageable pageable);

    Page<AutotraderCarListing> findBySoldDateBetweenAndStatus(String startDate, String endDate,String status, Pageable pageable);

}
