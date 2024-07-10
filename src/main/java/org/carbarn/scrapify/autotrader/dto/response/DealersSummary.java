package org.carbarn.scrapify.autotrader.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DealersSummary {
    private Long dealerId;
    private String state;
    private String tradingName;

    private Long soldQty;
    private Long totalListings;
    private Double listingVsSalesRatio;
    private Long currentLiveStock;

    public DealersSummary(Long dealerId, String state,String tradingName,  Long soldQty, Long totalListings,Long currentLiveStock) {
        this.dealerId = dealerId;
        this.state = state;
        this.tradingName=tradingName;
        this.soldQty = soldQty;
        this.totalListings = totalListings;
        this.currentLiveStock = currentLiveStock;
        this.listingVsSalesRatio = calculateRatio(soldQty, totalListings);
    }

    private Double calculateRatio(Long sold, Long listings) {
        return listings > 0 ? (double) sold / listings : 0.0;
    }

}
