package org.carbarn.scrapify.autotrader.services;

import org.carbarn.scrapify.autotrader.domain.Flag;
import org.carbarn.scrapify.autotrader.repositories.FlagRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class ScraperStatusService {

    private final FlagRepository flagRepository;

    @Autowired
    public ScraperStatusService(FlagRepository flagRepository) {
        this.flagRepository = flagRepository;
    }

    public String getScraperStatus(String scraperName) {
        if ("AUTOTRADER_SCRAPPER_STATUS".equals(scraperName)) {
            return flagRepository.findByName("AUTOTRADER_SCRAPPER_STATUS")
                    .map(Flag::getValue)
                    .orElse("Status not found");
        } else {
            return "Unknown scraper name";
        }
    }

    @Transactional
    public void switchScraperStatus(String status) {

        flagRepository.findByName("AUTOTRADER_SCRAPPER_STATUS")
                .ifPresentOrElse(
                        flag -> {
                            flag.setValue(status);
                            flagRepository.save(flag);
                        },
                        () -> flagRepository.save(new Flag("AUTOTRADER_SCRAPPER_STATUS", status))
                );
    }

    public Boolean fetchIsSoldStatus(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements scriptElements = doc.getElementsByTag("script");

            for (Element element : scriptElements) {
                String scriptText = element.data();
                if (scriptText.contains("dataLayer.push(")) {
                    String jsonData = scriptText.substring(scriptText.indexOf("{"), scriptText.lastIndexOf("}") + 1);
                    System.out.println(jsonData.contains("\"FEATURED\":1"));
//                    if (jsonData.contains("\"sold\":1")) {
                    if (jsonData.contains("\"FEATURED\":1")) {
                        return true;
                    } else if (jsonData.contains("\"sold\":0")) {
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Or handle this case as you see fit
    }
}
