package vn.cineprime.asm.ui.support;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;

/** Nền chung cho test Selenium + TestNG: mở/đóng trình duyệt cho mỗi test, chụp ảnh khi test lỗi (target/screenshots). */
public abstract class BaseUiTest {
    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeMethod(alwaysRun = true)
    public void openBrowser() {
        driver = DriverFactory.create();
        wait = new WebDriverWait(driver, Duration.ofSeconds(Config.waitSeconds()));
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser(ITestResult result) {
        try {
            if (driver != null && result.getStatus() == ITestResult.FAILURE) screenshot(result.getMethod().getMethodName());
        } finally {
            if (driver != null) driver.quit();
        }
    }

    protected void screenshot(String name) {
        try {
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            Files.write(dir.resolve(name + ".png"), ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
        } catch (IOException | WebDriverException ignored) {
            // chụp ảnh không được làm hỏng kết quả test
        }
    }

    protected static String uniqueEmail(String prefix) {
        return prefix + "-" + Long.toString(System.nanoTime(), 36) + "@cineprime.test";
    }
}
