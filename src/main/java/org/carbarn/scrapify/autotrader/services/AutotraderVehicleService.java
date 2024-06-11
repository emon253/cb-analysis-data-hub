package org.carbarn.scrapify.autotrader.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.carbarn.scrapify.autotrader.domain.AutotraderDealer;
import org.carbarn.scrapify.autotrader.dto.ProductFilterCriteria;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderDataAndSummary;
import org.carbarn.scrapify.autotrader.repositories.AutotraderDealerRepository;
import org.carbarn.scrapify.autotrader.repositories.AutotraderVehicleRepository;
import org.carbarn.scrapify.consts.ConstData;
import org.carbarn.scrapify.exceptions.ScrapifyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AutotraderVehicleService {

    private final AutotraderVehicleRepository repository;
    private final AutotraderDealerRepository dealerRepository;

    public AutotraderVehicleService(AutotraderVehicleRepository repository, AutotraderDealerRepository dealerRepository) {
        this.repository = repository;
        this.dealerRepository = dealerRepository;
    }

    @Transactional(readOnly = true)
    public Page<AutotraderCarListing> getAllVehicle(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<AutotraderCarListing> getAllVehicleByDealerId(Integer dealerId, Pageable pageable) {
        AutotraderDealer dealer = dealerRepository.findById(dealerId).orElseThrow(() -> new ScrapifyException("Dealer not found by id: " + dealerId));
        return repository.findByDealer(dealer, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AutotraderCarListing> getAllVehicleByAutotraderDealerId(Long dealerId, Pageable pageable) {
        return repository.getAllVehicleByAutoTraderDealerId(dealerId, pageable);
    }

    public AutotraderCarListing getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ScrapifyException("Vehicle not found by id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<AutotraderCarListing> getVehiclesByStatus(String status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    public Page<AutotraderCarListing> getVehiclesByVinStartsWith(String prefix, Pageable pageable) {
        return repository.findByVinStartingWith(prefix, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AutotraderCarListing> getVehiclesBySoldDateRange(String startDate, String endDate, Pageable pageable) {
        return repository.findBySoldDateBetweenAndStatus(startDate, endDate, "SOLD", pageable);
    }

    @Transactional(readOnly = true)
    public AutotraderDataAndSummary getFilteredVehiclesWithSummary(ProductFilterCriteria criteria, Pageable pageable) {
        Page<AutotraderCarListing> vehicles = repository.findAll(matchesCriteria(criteria), pageable);

        // Calculate live and sold counts using the same criteria but specific for status
        Long liveCount = repository.count(matchesCriteriaWithStatus(criteria, "LIVE"));
        Long soldCount = repository.count(matchesCriteriaWithStatus(criteria, "SOLD"));

        AutotraderDataAndSummary response = new AutotraderDataAndSummary();
        response.setVehicles(vehicles);
        response.setLive(liveCount);
        response.setSold(soldCount);

        return response;
    }

    private Specification<AutotraderCarListing> matchesCriteriaWithStatus(ProductFilterCriteria criteria, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>(Collections.singleton(matchesCriteria(criteria).toPredicate(root, query, criteriaBuilder)));
            predicates.add(criteriaBuilder.equal(root.get("status"), status));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<AutotraderCarListing> matchesCriteria(ProductFilterCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Handle makes
            if (criteria.getMakes() != null && !criteria.getMakes().isEmpty()) {
                List<Predicate> makePredicates = new ArrayList<>();
                for (String make : criteria.getMakes()) {
                    makePredicates.add(criteriaBuilder.like(root.get("make"), "%" + make + "%"));
                }
                predicates.add(criteriaBuilder.or(makePredicates.toArray(new Predicate[0])));
            }

            // Handle models
            if (criteria.getModels() != null && !criteria.getModels().isEmpty()) {
                List<Predicate> modelPredicates = new ArrayList<>();
                for (String model : criteria.getModels()) {
                    modelPredicates.add(criteriaBuilder.like(root.get("model"), "%" + model + "%"));
                }
                predicates.add(criteriaBuilder.or(modelPredicates.toArray(new Predicate[0])));
            }

            if (criteria.getShowListedDealersVehicles()) {
                List<Predicate> modelPredicates = new ArrayList<>();
                for (Long dealerId : ConstData.getDealers()) {
                    modelPredicates.add(criteriaBuilder.equal(root.get("autoTraderDealerId"), dealerId));
                }
                predicates.add(criteriaBuilder.or(modelPredicates.toArray(new Predicate[0])));
            }

            // Handle year, minYear and maxYear
            if (criteria.getYear() != null) {
                predicates.add(criteriaBuilder.equal(root.get("manufactureYear"), criteria.getYear()));
            }
            if (criteria.getMinYear() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("manufactureYear"), criteria.getMinYear()));
            }
            if (criteria.getMaxYear() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("manufactureYear"), criteria.getMaxYear()));
            }

            if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getDealerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("autoTraderDealerId"), criteria.getDealerId()));
            }

            if (criteria.getVinPrefix() != null) {
                predicates.add(criteriaBuilder.like(root.get("vin"), criteria.getVinPrefix() + "%"));
            }

            // Assuming soldDate is a string in format 'YYYY-MM-DD HH:MM:SS'
            Expression<String> dateStringExpr = criteriaBuilder.function("DATE", String.class, root.get("soldDate"));
            Expression<String> createdDateExpr = criteriaBuilder.function("DATE", String.class, root.get("createdAt"));
            if (criteria.getMinListingDate() != null && criteria.getMaxListingDate() != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.between(createdDateExpr,
                                criteria.getMinListingDate(),
                                criteria.getMaxListingDate()),
                        criteriaBuilder.between(dateStringExpr,
                                criteria.getMinListingDate(),
                                criteria.getMaxListingDate())
                ));
            } else if (criteria.getMinListingDate() != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.greaterThanOrEqualTo(createdDateExpr, criteria.getMinListingDate()),
                        criteriaBuilder.greaterThanOrEqualTo(dateStringExpr, criteria.getMinListingDate())
                ));
            } else if (criteria.getMaxListingDate() != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.lessThanOrEqualTo(createdDateExpr, criteria.getMaxListingDate()),
                        criteriaBuilder.lessThanOrEqualTo(dateStringExpr, criteria.getMaxListingDate())
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
