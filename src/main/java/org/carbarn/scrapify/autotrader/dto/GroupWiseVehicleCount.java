package org.carbarn.scrapify.autotrader.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupWiseVehicleCount {
    private String make;
    private String model;
    private Integer manufactureYear;
    private Long totalSales;
}
