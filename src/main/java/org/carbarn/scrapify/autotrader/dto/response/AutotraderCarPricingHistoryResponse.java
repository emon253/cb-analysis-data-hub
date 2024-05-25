package org.carbarn.scrapify.autotrader.dto.response;

import lombok.Data;

@Data
public class AutotraderCarPricingHistoryResponse {
    private int advertised_price;
    private int egc_price;
    private int driveaway_price;
    private int is_fixed_price;
    private String deleted_at;
}
