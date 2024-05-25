package org.carbarn.scrapify;

import org.carbarn.scrapify.autotrader.domain.Flag;
import org.carbarn.scrapify.autotrader.repositories.FlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScrapifyApplication implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(ScrapifyApplication.class, args);
    }


    @Override
    public void run(String... args) throws InterruptedException {
//		scraperClient.runScraper();
//        scraperClient.startScraperForDearWiseData();
        ensureScrapperStatusFlag();
    }

    @Autowired
    private FlagRepository flagRepository;
    public void ensureScrapperStatusFlag() {
        // Attempt to retrieve the flag, and create/save it as "STOPPED" if not present
        Flag flag = flagRepository.findByName("AUTOTRADER_SCRAPPER_STATUS")
                .orElseGet(() -> {
                    Flag newFlag = new Flag();
                    newFlag.setName("AUTOTRADER_SCRAPPER_STATUS");
                    newFlag.setValue("STOPPED");
                    return flagRepository.save(newFlag);
                });
    }
}
