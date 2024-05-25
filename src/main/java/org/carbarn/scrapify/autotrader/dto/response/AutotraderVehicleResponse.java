package org.carbarn.scrapify.autotrader.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AutotraderVehicleResponse {
    private int current_page;
    private int last_page;
    private int total;
    private String next_page_url;
    private List<AutotraderApiResponse> vehicles = new ArrayList<>();

//    public Boolean hasNextPage(){
//        return current_page <= (total/13);
//    }
    public Boolean hasNextPage(){
        return !vehicles.isEmpty();
    }
}
