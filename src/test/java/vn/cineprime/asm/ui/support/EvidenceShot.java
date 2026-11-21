package vn.cineprime.asm.ui.support;

import java.nio.file.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import vn.cineprime.asm.ui.pages.*;

/**
 * Công cụ chụp ảnh minh chứng (không phải test, không nằm trong suite): đăng nhập Admin, mở menu ba chấm khi bảng chỉ có 1 dòng
 * và lưu ảnh để so sánh trước/sau khi sửa BUG-001.
 * Chạy: java -cp "target/test-classes;target/classes;&lt;classpath&gt;" vn.cineprime.asm.ui.support.EvidenceShot ten-anh.png "Hoạt hình"
 */
public class EvidenceShot {
    public static void main(String[] args) throws Exception {
        WebDriver driver = DriverFactory.create();
        try {
            WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(Config.waitSeconds()));
            new LoginPage(driver, wait).loginAs(Config.adminEmail(), Config.demoPassword());
            AdminGenresPage page = new AdminGenresPage(driver, wait).open().search(args.length > 1 ? args[1] : "Hoạt hình");
            WebElement summary = driver.findElement(By.cssSelector("details.row-menu summary"));
            summary.click();
            Thread.sleep(600);
            Path out = Path.of(args[0]);
            Files.createDirectories(out.toAbsolutePath().getParent());
            Files.write(out, ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
            System.out.println("Đã lưu " + out.toAbsolutePath() + " với " + page.rowCount() + " dòng");
        } finally {
            driver.quit();
        }
    }
}
