package vn.cineprime.asm.ui.support;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

/** Tạo WebDriver. Selenium Manager (có sẵn trong Selenium 4.6+) tự tải chromedriver/msedgedriver đúng phiên bản, không cần WebDriverManager. */
public final class DriverFactory {
    private DriverFactory() {}

    public static WebDriver create() {
        WebDriver driver;
        if (Config.browser().equals("edge")) {
            EdgeOptions o = new EdgeOptions();
            if (Config.headless()) o.addArguments("--headless=new");
            o.addArguments("--window-size=1366,768", "--lang=vi-VN", "--disable-gpu");
            driver = new EdgeDriver(o);
        } else {
            ChromeOptions o = new ChromeOptions();
            if (Config.headless()) o.addArguments("--headless=new");
            o.addArguments("--window-size=1366,768", "--lang=vi-VN", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new ChromeDriver(o);
        }
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        return driver;
    }
}
