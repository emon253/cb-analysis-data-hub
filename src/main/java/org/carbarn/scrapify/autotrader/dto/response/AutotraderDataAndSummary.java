package org.carbarn.scrapify.autotrader.dto.response;

import lombok.Data;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.springframework.data.domain.Page;

@Data
public class AutotraderDataAndSummary {
    private Page<AutotraderCarListing> vehicles;
    private Long live;
    private Long sold;

}
