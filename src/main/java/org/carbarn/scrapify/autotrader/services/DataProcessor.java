package org.carbarn.scrapify.autotrader.services;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderApiResponse;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderVehicleResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Log4j2
@Component
public class DataProcessor {
    private final Gson gson = new Gson();

    public AutotraderVehicleResponse processResponse(String jsonResponse) {
        AutotraderVehicleResponse autotraderVehicleResponse = new AutotraderVehicleResponse();
        List<AutotraderApiResponse> responseList = new ArrayList<>();
        try {
            JSONObject response = new JSONObject(jsonResponse);
            JSONArray data = response.getJSONArray("data");
            int currentPage = response.getInt("current_page");
            int lastPage = response.getInt("last_page");
            int total = response.getInt("total");

            for (int i = 0; i < data.length(); i++) {
                AutotraderApiResponse processedData = parseVehicle(data.getJSONObject(i));
                responseList.add(processedData);
            }
            autotraderVehicleResponse.setVehicles(responseList);
            autotraderVehicleResponse.setCurrent_page(currentPage);
            autotraderVehicleResponse.setLast_page(lastPage);
            autotraderVehicleResponse.setTotal(total);
            return autotraderVehicleResponse;

        } catch (Exception e) {
            log.error("Error processing response: ", e);
            return null;
        }
    }
    public AutotraderApiResponse processResponseForObject(String jsonResponse) {
        try {
            JSONObject response = new JSONObject(jsonResponse);
            JSONObject data = response.getJSONObject("data");
            return gson.fromJson(data.toString(), AutotraderApiResponse.class);
        } catch (Exception e) {
            log.error("Error processing response: ", e);
            return null;
        }
    }


    private AutotraderApiResponse parseVehicle(JSONObject vehicle) {
        try {
            JSONObject source = vehicle.getJSONObject("_source");
            return gson.fromJson(source.toString(), AutotraderApiResponse.class);
        } catch (Exception e) {
            log.error("Error parsing vehicle data: ", e);
            return null;
        }
    }

//    private Boolean isListedDealer(Integer dealerId) {return getDealers().contains(dealerId);}



}
