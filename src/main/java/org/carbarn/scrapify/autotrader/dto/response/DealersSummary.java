package org.carbarn.scrapify.autotrader.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class DealersSummary {
    private Long dealerId;
    private String state;
    private String tradingName;

    private Long soldQty;
    private Long totalListings;
    private Double listingVsSalesRatio;
    private Long currentLiveStock;
    private Boolean enableDataScraping;

    public DealersSummary(Long dealerId, String state,String tradingName,Boolean enableDataScraping,  Long soldQty, Long totalListings,Long currentLiveStock) {
        this.dealerId = dealerId;
        this.state = state;
        this.tradingName=tradingName;
        this.soldQty = soldQty;
        this.totalListings = totalListings;
        this.currentLiveStock = currentLiveStock;
        this.listingVsSalesRatio = calculateRatio(soldQty, totalListings);
        this.enableDataScraping = enableDataScraping;
    }

    public DealersSummary( Long soldQty, Long totalListings,Long currentLiveStock) {
        this.soldQty = soldQty;
        this.totalListings = totalListings;
        this.currentLiveStock = currentLiveStock;
        this.listingVsSalesRatio = calculateRatio(soldQty, totalListings);
    }

    private Double calculateRatio(Long sold, Long listings) {
        return listings > 0 ? (double) sold / listings : 0.0;
    }

}
