package org.carbarn.scrapify.autotrader.repositories;

import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.carbarn.scrapify.autotrader.domain.AutotraderDealer;
import org.carbarn.scrapify.autotrader.dto.response.DealersSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutotraderVehicleRepository extends JpaRepository<AutotraderCarListing, Long>, JpaSpecificationExecutor<AutotraderCarListing> {

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

    List<AutotraderCarListing> findByStatus(String status);


    Page<AutotraderCarListing> findByVinStartingWith(String prefix, Pageable pageable);

    Page<AutotraderCarListing> findBySoldDateBetweenAndStatus(String startDate, String endDate, String status, Pageable pageable);

    //    @Query("SELECT new org.carbarn.scrapify.autotrader.dto.response.DealersSummary(dealer.autoTraderDealerId, dealer.state,dealer.tradingName, " +
//            "COUNT(listing.id) AS soldQty, " +
//            "CAST((SELECT COUNT(dl.id) FROM AutotraderCarListing dl WHERE dl.dealer.autoTraderDealerId = dealer.autoTraderDealerId AND dl.status = 'Live' AND (dl.createdAt BETWEEN :startDate AND :endDate)) AS long) AS totalListings, " +
//            "CAST((SELECT COUNT(dl.id) FROM AutotraderCarListing dl WHERE dl.dealer.autoTraderDealerId = dealer.autoTraderDealerId AND dl.status = 'Live') AS long) AS currentLiveStock) " +
//            "FROM AutotraderCarListing listing " +
//            "JOIN listing.dealer dealer " +
//            "WHERE listing.status = 'SOLD' AND (listing.soldDate BETWEEN :startDate AND :endDate) " +
//            "AND (:dealerId IS NULL OR dealer.autoTraderDealerId = :dealerId) " +
//            "GROUP BY dealer.autoTraderDealerId, dealer.state,dealer.tradingName ")
//    Page<DealersSummary> getDealerSalesSummary(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("dealerId") Long dealerId, Pageable pageable);
//    @Query("SELECT new org.carbarn.scrapify.autotrader.dto.response.DealersSummary(dealer.autoTraderDealerId, dealer.state, dealer.tradingName, " +
//            "COUNT(CASE WHEN listing.status = 'SOLD' THEN 1 END) AS soldQty, " +
//            "CAST((SELECT COUNT(dl.id) FROM AutotraderCarListing dl WHERE dl.dealer.autoTraderDealerId = dealer.autoTraderDealerId AND dl.status = 'Live' AND (dl.createdAt BETWEEN :startDate AND :endDate) AND (:vinPrefix IS NULL OR dl.vin LIKE CONCAT(:vinPrefix, '%'))) AS long) AS totalListings, " +
//            "CAST((SELECT COUNT(dl.id) FROM AutotraderCarListing dl WHERE dl.dealer.autoTraderDealerId = dealer.autoTraderDealerId AND dl.status = 'Live' AND (:vinPrefix IS NULL OR dl.vin LIKE CONCAT(:vinPrefix, '%'))) AS long) AS currentLiveStock) " +
//            "FROM AutotraderCarListing listing " +
//            "JOIN listing.dealer dealer " +
//            "WHERE listing.status = 'SOLD' AND (listing.soldDate BETWEEN :startDate AND :endDate) " +
//            "AND (:dealerId IS NULL OR dealer.autoTraderDealerId = :dealerId) " +
//            "AND (:vinPrefix IS NULL OR listing.vin LIKE CONCAT(:vinPrefix, '%')) " +
//            "GROUP BY dealer.autoTraderDealerId, dealer.state, dealer.tradingName")
//    Page<DealersSummary> getDealerSalesSummary(
//            @Param("startDate") String startDate,
//            @Param("endDate") String endDate,
//            @Param("dealerId") Long dealerId,
//            @Param("vinPrefix") String vinPrefix,
//            Pageable pageable);


    @Query("SELECT new org.carbarn.scrapify.autotrader.dto.response.DealersSummary(" +
            "dealer.autoTraderDealerId, " +
            "dealer.state, " +
            "dealer.tradingName, " +
            "dealer.enableDataScraping, " +
            "COUNT(CASE WHEN listing.status = 'SOLD' THEN 1 END) AS soldQty, " +
            "CAST((SELECT COUNT(dl.id) FROM AutotraderCarListing dl WHERE dl.dealer.autoTraderDealerId = dealer.autoTraderDealerId " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR dl.createdAt BETWEEN :startDate AND :endDate) " +
            "AND (:vinPrefix IS NULL OR dl.vin LIKE CONCAT(:vinPrefix, '%'))) AS long) AS totalListings, " +
            "CAST((SELECT COUNT(dl.id) FROM AutotraderCarListing dl WHERE dl.dealer.autoTraderDealerId = dealer.autoTraderDealerId " +
            "AND dl.status = 'Live' " +
            "AND (:vinPrefix IS NULL OR dl.vin LIKE CONCAT(:vinPrefix, '%'))) AS long) AS currentLiveStock) " +
            "FROM AutotraderCarListing listing " +
            "JOIN listing.dealer dealer " +
            "WHERE (:startDate IS NULL OR :endDate IS NULL OR listing.soldDate BETWEEN :startDate AND :endDate) " +
            "AND (:dealerId IS NULL OR dealer.autoTraderDealerId = :dealerId) " +
            "AND (:vinPrefix IS NULL OR listing.vin LIKE CONCAT(:vinPrefix, '%')) " +
            "GROUP BY dealer.autoTraderDealerId, dealer.state, dealer.tradingName, dealer.enableDataScraping")
    Page<DealersSummary> getDealerSalesSummary(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("dealerId") Long dealerId,
            @Param("vinPrefix") String vinPrefix,
            Pageable pageable);




}
