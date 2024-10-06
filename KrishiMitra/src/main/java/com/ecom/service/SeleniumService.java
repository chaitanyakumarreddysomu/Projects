package com.ecom.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeleniumService {

    public MarketData getMarketData(String CommSelect) {
    	
    	ChromeOptions opt = new ChromeOptions();
    	opt.addArguments("--headless=new");
    	
        WebDriver driver = new ChromeDriver(opt);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        try {
            driver.get("https://agmarknet.gov.in/SearchCmmMkt.aspx");
            driver.manage().window().maximize();

            // Close the popup if it appears
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            try {
                WebElement close = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@class='close']")));
                close.click();
            } catch (Exception e) {
                // No popup to close
            }

            // Fetch commodity options
            Select commoditySelect = new Select(driver.findElement(By.id("ddlCommodity")));
            List<WebElement> commodities = commoditySelect.getOptions();
            List<String> commodityNames = commodities.stream()
                    .map(WebElement::getText)
                    .collect(Collectors.toList());

            // Select the specified commodity and fetch market data
            commoditySelect.selectByVisibleText(CommSelect);
            driver.findElement(By.id("btnGo")).click();

            // Wait for the price data to load
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cphBody_GridPriceData")));

            // Fetch price data
            List<WebElement> priceElements = driver.findElements(By.xpath("//table[@id='cphBody_GridPriceData']//tr//td[7]"));

            return extractMarketData(priceElements, commodityNames);

        } finally {
            driver.quit(); // Ensure the driver is closed
        }
    }

    private MarketData extractMarketData(List<WebElement> priceElements, List<String> commodities) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int sum = 0;
        int count = 0;

        for (WebElement priceElement : priceElements) {
            String text = priceElement.getText().trim();
            if (!text.isEmpty()) {
                try {
                    int value = Integer.parseInt(text);
                    sum += value;
                    count++;
                    max = Math.max(max, value);
                    min = Math.min(min, value);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid price data: " + text);
                }
            }
        }

        int avg = (count > 0) ? sum / count : 0;

        // Handling cases where no valid prices were found
        if (count == 0) {
            max = 0;
            min = 0;	
        }

        return new MarketData(max, min, avg, commodities);
    }

    public static class MarketData {
        private int max;
        private int min;
        private int avg;
        private List<String> commodities;

        public MarketData(int max, int min, int avg, List<String> commodities) {
            this.max = max;
            this.min = min;
            this.avg = avg;
            this.commodities = commodities;
        }

        public int getMax() {
            return max;
        }

        public int getMin() {
            return min;
        }

        public int getAvg() {
            return avg;
        }

        public List<String> getCommodities() {
            return commodities;
        }
    }
}
