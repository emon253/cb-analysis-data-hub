package org.carbarn.scrapify.autotrader.dto.response;


import lombok.Data;

@Data
public class AutotraderApiResponse {
    private Long id;
    private String source;
    private Long dealer_id;
    private Integer source_id;
    private String vin;
    private String stock_no;
    private int manu_year;
    private String colour_body;
    private Integer odometer;
    private String rego;
    private String rego_expiry;
    private String make;
    private String model;
    private String variant;
    private String series;
    private String status;
    private String url;
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private AutotraderCarPriceResponse price;
    private AutotraderCarPricingHistoryResponse pricingHistory;
    private AutotraderDealerResponse dealer;
    private AutotraderLocationResponse location;
}
