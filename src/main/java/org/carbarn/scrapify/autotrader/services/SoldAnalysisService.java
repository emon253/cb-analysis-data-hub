package org.carbarn.scrapify.autotrader.services;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.Data;
import org.carbarn.scrapify.autotrader.dto.GroupWiseVehicleCount;
import org.carbarn.scrapify.autotrader.repositories.AutotraderVehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class SoldAnalysisService {
    private final AutotraderVehicleRepository repository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public SoldAnalysisService(AutotraderVehicleRepository repository) {
        this.repository = repository;
    }

    public List<GroupWiseVehicleCount> getSalesSummary(String groupBy, String soldDateStart, String soldDateEnd, String vinPrefix) {
        StringBuilder baseQuery = new StringBuilder("SELECT ");

        // Dynamically build the select clause
        List<String> groupByFields = new ArrayList<>();
        if (groupBy.contains("make")) {
            baseQuery.append("a.make AS make, ");
            groupByFields.add("a.make");
        } else {
            baseQuery.append("'' AS make, ");
        }

        if (groupBy.contains("model")) {
            baseQuery.append("a.model AS model, ");
            groupByFields.add("a.model");
        } else {
            baseQuery.append("'' AS model, ");
        }

        if (groupBy.contains("manufactureYear")) {
            baseQuery.append("a.manu_year AS manufactureYear, ");
            groupByFields.add("a.manu_year");
        } else {
            baseQuery.append("null AS manufactureYear, ");
        }

        baseQuery.append("COUNT(a.id) AS totalSales ");
        baseQuery.append("FROM autotrader_vehicle a ");
        baseQuery.append("WHERE a.sold_date BETWEEN :soldDateStart AND :soldDateEnd ");

        // Add the VIN prefix filter if provided
        if (vinPrefix != null && !vinPrefix.trim().isEmpty()) {
            baseQuery.append("AND a.vin LIKE :vinPrefix ");
        }

        // Dynamically build the group by clause
        if (!groupByFields.isEmpty()) {
            baseQuery.append("GROUP BY ");
            baseQuery.append(String.join(", ", groupByFields));
        }

        baseQuery.append(" ORDER BY totalSales DESC");

        // Create the query
        Query query = entityManager.createNativeQuery(baseQuery.toString());
        query.setParameter("soldDateStart", soldDateStart);
        query.setParameter("soldDateEnd", soldDateEnd);

        // Set the VIN prefix parameter if provided
        if (vinPrefix != null && !vinPrefix.trim().isEmpty()) {
            query.setParameter("vinPrefix", vinPrefix + "%"); // Use % for 'starts with' condition
        }

        // Get scalar values and map them to DTO
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> new GroupWiseVehicleCount(
                        (String) row[0],  // make
                        (String) row[1],  // model
                        (Integer) row[2], // manufactureYear
                        (Long) row[3]     // totalSales
                ))
                .collect(Collectors.toList());
    }
}
