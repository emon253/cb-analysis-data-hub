package org.carbarn.scrapify.autotrader.services;

import lombok.extern.log4j.Log4j2;
import org.carbarn.scrapify.autotrader.domain.Flag;
import org.carbarn.scrapify.autotrader.dto.response.AutotraderVehicleResponse;
import org.carbarn.scrapify.autotrader.repositories.FlagRepository;
import org.carbarn.scrapify.consts.ConstData;
import org.carbarn.scrapify.exceptions.ScrapifyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Log4j2
@Service
public class AutotraderScraperService {

    @Value("${autotrader.base.url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final ScraperStatusService statusService;
    private final AutotraderPersistenceService persistenceService;
    private final FlagRepository flagRepository;
    private final DataProcessor dataProcessor;
    private final VehicleSoldUpdateService soldUpdateService;

    @Value("${scraping.interval.autotrader}")
    private Long interval;

    public AutotraderScraperService(RestTemplate restTemplate,
                                    ScraperStatusService statusService,
                                    AutotraderPersistenceService persistenceService,
                                    FlagRepository flagRepository,
                                    DataProcessor dataProcessor,
                                    VehicleSoldUpdateService soldUpdateService) {
        this.restTemplate = restTemplate;
        this.statusService = statusService;
        this.persistenceService = persistenceService;
        this.flagRepository = flagRepository;
        this.dataProcessor = dataProcessor;
        this.soldUpdateService = soldUpdateService;
    }

    public void startScraperForDearWiseData(List<Long> dealers) throws InterruptedException {
//        System.out.println(statusService.getScraperStatus("AUTOTRADER_SCRAPPER_STATUS"));
//        if (!statusService.getScraperStatus("AUTOTRADER_SCRAPPER_STATUS").equals("RUNNING")) {
//            log.info("Scraper is set to STOPPED. Exiting...");
//            return;
//        }

        long currentPage = getLastPageFlagValue();

        for (int i = getLastDealerFlagValue(); i < dealers.size(); i++) {
            long dealerId = dealers.get(i);
            log.info("Initiating scraping for dealer: index: {}, id: {}", i, dealerId);
            boolean hasNextPage = true;
            while (hasNextPage) {
                String requestUrl = baseUrl + "?page=" + currentPage + "&dealer_id=" + dealerId + "&ipLookup=1&sorting_variation=smart_sort_2&paginate=26";
                Thread.sleep(interval);

                try {
                    log.info("Requesting page number {} from URL: {}", currentPage, requestUrl);
                    ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
                    if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                        AutotraderVehicleResponse vehicleResponse = dataProcessor.processResponse(response.getBody());

                        log.info("Response success found {} entries for page {} with {} vehicles", vehicleResponse.getVehicles().size(), vehicleResponse.getCurrent_page(), vehicleResponse);
                        persistenceService.persistVehicleInformation(vehicleResponse.getVehicles());
                        hasNextPage = vehicleResponse.hasNextPage();
                        currentPage = vehicleResponse.getCurrent_page();
                        updateLastPageFlag(currentPage);
                        updateLastDealerFlag(i);
                    } else {
                        log.info("No more data available or failed to fetch data.");
                        hasNextPage = false;
                    }
                    if (hasNextPage) {
                        currentPage += 1;
                    }
                } catch (HttpClientErrorException.TooManyRequests e) {
                    log.warn("Rate limiting error: Too many requests. Waiting before retrying...");
                } catch (Exception e) {
                    log.error("Error fetching data: ", e);
                    if (hasNextPage) {
                        updateLastPageFlag(currentPage + 1);
                        startScraperForDearWiseData(dealers);
                    }
                }
            }
            currentPage = 1;
        }
//        soldUpdateService.updateDealerWiseVehicleSoldStatus();
        updateLastPageFlag(1);
        updateLastDealerFlag(0);
        log.info("Scraping process is complete. All pages for all dealers have been processed.");
    }

    public void startScraperPageWiseData() {

        long currentPage = getLastPageFlagValue();
        boolean hasNextPage = true;

        try {
            while (hasNextPage) {

                String requestUrl = baseUrl + "?page=" + currentPage + "&ipLookup=1&sorting_variation=smart_sort_2&paginate=26";
                log.info("Requesting page number {} from URL: {}", currentPage, requestUrl);
                Thread.sleep(interval);

                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                    AutotraderVehicleResponse vehicleResponse = dataProcessor.processResponse(response.getBody());
                    log.info("Response success found {} entries for page {} with {} vehicles", vehicleResponse.getVehicles().size(), vehicleResponse.getCurrent_page(), vehicleResponse);
                    persistenceService.persistVehicleInformation(vehicleResponse.getVehicles());
                    hasNextPage = vehicleResponse.hasNextPage();
                    if (hasNextPage) {
                        currentPage = vehicleResponse.getCurrent_page() + 1;
                    } else {
                        currentPage = 1;
                    }
                    updateLastPageFlag(currentPage);
                } else {
                    log.info("No more data available or failed to fetch data.");
                    hasNextPage = false;
                }
            }
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Rate limiting error: Too many requests. Waiting before retrying...");
        } catch (Exception e) {
            log.error("Error fetching data: ", e);
            if (e.getMessage().contains("Elasticsearch result window is too large")) {
                log.info("Page-wise scraping process is complete.");
                updateLastPageFlag(1);
                return;
            }
        }
        updateLastPageFlag(1);
        log.info("Page-wise scraping process is complete.");
    }

    private long getLastPageFlagValue() {
        return flagRepository.findByName("lastPage")
                .map(flag -> Long.parseLong(flag.getValue()))
                .orElse(1L);
    }

    private int getLastDealerFlagValue() {
        return flagRepository.findByName("lastDealer")
                .map(flag -> Integer.parseInt(flag.getValue()))
                .orElse(1);
    }

    private void updateLastPageFlag(long currentPage) {
        flagRepository.findByName("lastPage").ifPresentOrElse(
                flag -> {
                    flag.setValue(String.valueOf(currentPage));
                    flagRepository.save(flag);
                },
                () -> flagRepository.save(new Flag("lastPage", String.valueOf(currentPage)))
        );
    }

    private void updateLastDealerFlag(int lastDealer) {
        flagRepository.findByName("lastDealer").ifPresentOrElse(
                flag -> {
                    flag.setValue(String.valueOf(lastDealer));
                    flagRepository.save(flag);
                },
                () -> flagRepository.save(new Flag("lastDealer", String.valueOf(lastDealer)))
        );
    }
}
