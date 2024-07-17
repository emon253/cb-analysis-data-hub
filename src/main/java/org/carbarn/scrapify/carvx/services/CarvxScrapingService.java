package org.carbarn.scrapify.carvx.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.carbarn.scrapify.autotrader.domain.AutotraderCarListing;
import org.carbarn.scrapify.autotrader.repositories.AutotraderVehicleRepository;
import org.carbarn.scrapify.carvx.entity.CarvxModelCodeInfo;
import org.carbarn.scrapify.carvx.repositories.CarvxModelCodeInfoRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CarvxScrapingService {

    private static final Logger logger = LogManager.getLogger(CarvxScrapingService.class);

    @Value("${CHROME_DRIVER_PATH}")
    private String CHROME_DRIVER_PATH;
    private static final String TARGET_URL = "https://carvx.jp/";

    private final AutotraderVehicleRepository vehicleRepository;
    private final CarvxModelCodeInfoRepository carvxModelCodeInfoRepository;
    private final AtomicBoolean running = new AtomicBoolean(false); // Control flag

    public CarvxScrapingService(AutotraderVehicleRepository vehicleRepository, CarvxModelCodeInfoRepository carvxModelCodeInfoRepository) {
        this.vehicleRepository = vehicleRepository;
        this.carvxModelCodeInfoRepository = carvxModelCodeInfoRepository;
    }

    private int batchSize = 100;
    private int batchNumber = 1;
    private int totalPages = 0;

    @Async
    public CompletableFuture<Void> performScraping() {
        logger.info("Starting scraping task");
        running.set(true); // Set running flag to true
        Pageable pageable = PageRequest.of(0, batchSize);
        Page<AutotraderCarListing> vehiclePage;

        do {
            if (!running.get()) { // Check if the process should stop
                logger.info("Scraping process stopped.");
                break;
            }

            vehiclePage = vehicleRepository.findByChassisIsNull(pageable);
            totalPages = vehiclePage.getTotalPages();
            List<AutotraderCarListing> vehicles = vehiclePage.getContent();
            logger.info("Processing batch no {} of {} found {} vehicles", batchNumber, totalPages, vehicles.size());

            scrapeAndSaveData(vehicles);

            pageable = vehiclePage.nextPageable();
            batchNumber++;
        } while (vehiclePage.hasNext());

        running.set(false); // Reset running flag to false
        return CompletableFuture.completedFuture(null);
    }

    public void stopScraping() {
        running.set(false); // Method to stop the scraping process
    }

    private void scrapeAndSaveData(List<AutotraderCarListing> vehicles) {
        WebDriver driver = null;
        List<Map<String, String>> resultsList = new ArrayList<>();

        try {
            driver = initializeWebDriver();

            for (AutotraderCarListing vehicle : vehicles) {
                if (!running.get()) { // Check if the process should stop
                    logger.info("Scraping process stopped.");
                    break;
                }

                String chassisId = extractChassisFromVin(vehicle.getVin());
                Optional<Map<String, String>> scrapedData = processChassisId(driver, chassisId);

                // Update vehicle with chassisId and possibly scraped data
                vehicle.setChassis(chassisId);
                scrapedData.ifPresent(data -> {
                    vehicle.setModelCode(data.get("Body"));  // Body is modelCode
                    resultsList.add(data);
                });

                vehicleRepository.save(vehicle);
            }

            // Save scraped data to CarvxModelCodeInfo table
            saveScrapedData(resultsList);

        } catch (Exception e) {
            logger.error("Scraping process failed: {}", e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private WebDriver initializeWebDriver() {
        System.setProperty("webdriver.chrome.driver", getClass().getClassLoader().getResource(CHROME_DRIVER_PATH).getPath());

        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-web-security");

        return new ChromeDriver(options);
    }

    private Optional<Map<String, String>> processChassisId(WebDriver driver, String chassisId) {
        try {
            driver.get(TARGET_URL);

            WebElement inputField = driver.findElement(By.name("chassis_number"));
            inputField.sendKeys(chassisId);

            WebElement submitButton = driver.findElement(By.className("searchButton"));
            submitButton.click();

            Thread.sleep(5000);

            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);

            return extractVehicleDetails(doc, chassisId);
        } catch (Exception e) {
            logger.error("Error processing chassis ID {}: {}", chassisId, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Map<String, String>> extractVehicleDetails(Document doc, String chassisId) {
        Elements carTitleTags = doc.select("a.carTitle");
        if (!carTitleTags.isEmpty()) {
            try {
                // If multiple records are found, select the first one for simplicity
                Element carTitleTag = carTitleTags.first();
                String vehicleDetails = carTitleTag.text().trim();

                Map<String, String> details = Stream.of(
                        new AbstractMap.SimpleEntry<>("Chassis", chassisId),  // Chassis is from VIN, not scrapped data
                        new AbstractMap.SimpleEntry<>("Body", extractField(doc, carTitleTag, 2)),  // Body is modelCode
                        new AbstractMap.SimpleEntry<>("Model", extractField(doc, carTitleTag, 3)),
                        new AbstractMap.SimpleEntry<>("Engine", extractField(doc, carTitleTag, 4)),
                        new AbstractMap.SimpleEntry<>("Grade", extractField(doc, carTitleTag, 5)),
                        new AbstractMap.SimpleEntry<>("Drive", extractField(doc, carTitleTag, 6)),
                        new AbstractMap.SimpleEntry<>("Year", extractField(doc, carTitleTag, 7)),
                        new AbstractMap.SimpleEntry<>("Transmission", extractField(doc, carTitleTag, 8)),
                        new AbstractMap.SimpleEntry<>("Fuel", extractField(doc, carTitleTag, 9))
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                details.put("SearchValue", chassisId);
                logger.warn("Vehicle details found for chassis ID: {}", chassisId);

                return Optional.of(details);
            } catch (Exception e) {
                logger.error("Error extracting vehicle details for chassis ID {}: {}", chassisId, e.getMessage());
            }
        } else {
            logger.warn("Vehicle details not found for chassis ID: {}", chassisId);
        }
        return Optional.empty();
    }

    private String extractField(Document doc, Element carTitleTag, int fieldIndex) {
        try {
            // Adjust the selector to extract the field relative to the carTitleTag
            Element fieldElement = carTitleTag.closest("div")
                    .select("div:nth-of-type(" + fieldIndex + ")")
                    .first();
            return fieldElement.text().trim().split(": ")[1];
        } catch (Exception e) {
            logger.error("Error extracting field index {}: {}", fieldIndex, e.getMessage());
            return "";
        }
    }

    private String extractChassisFromVin(String vin) {
        if (vin.length() == 17) {
            return vin.substring(4).replaceAll("^0+", ""); // Extract last 13 characters and remove leading zeros
        } else {
            throw new IllegalArgumentException("Invalid VIN: " + vin);
        }
    }

    private void saveScrapedData(List<Map<String, String>> resultsList) {
        List<CarvxModelCodeInfo> scrapedDataList = resultsList.stream()
                .map(this::mapToCarvxModelCodeInfo)
                .collect(Collectors.toList());
        carvxModelCodeInfoRepository.saveAll(scrapedDataList);
    }

    private CarvxModelCodeInfo mapToCarvxModelCodeInfo(Map<String, String> dataMap) {
        CarvxModelCodeInfo carvxModelCodeInfo = new CarvxModelCodeInfo();
        carvxModelCodeInfo.setSearchValue(dataMap.get("SearchValue"));
        carvxModelCodeInfo.setChassis(dataMap.get("Chassis"));
        carvxModelCodeInfo.setBody(dataMap.get("Body"));
        carvxModelCodeInfo.setModel(dataMap.get("Model"));
        carvxModelCodeInfo.setEngine(dataMap.get("Engine"));
        carvxModelCodeInfo.setGrade(dataMap.get("Grade"));
        carvxModelCodeInfo.setDrive(dataMap.get("Drive"));
        carvxModelCodeInfo.setYear(dataMap.get("Year"));
        carvxModelCodeInfo.setTransmission(dataMap.get("Transmission"));
        carvxModelCodeInfo.setFuel(dataMap.get("Fuel"));
        return carvxModelCodeInfo;
    }
}
