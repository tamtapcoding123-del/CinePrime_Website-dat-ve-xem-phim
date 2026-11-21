package vn.cineprime.asm.ui.tests;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.*;
import vn.cineprime.asm.ui.support.*;

/** Y4 - Selenium + TestNG: các trang công khai của khách (smoke). */
public class PublicPagesUiTest extends BaseUiTest {

    @DataProvider(name = "publicPaths")
    public Object[][] publicPaths() { return new Object[][]{{"/"}, {"/movies"}, {"/showtimes"}, {"/cinemas"}, {"/offers"}, {"/login"}, {"/register"}}; }

    @Test(groups = {"smoke", "public"}, dataProvider = "publicPaths", description = "ID093 Trang công khai mở được, có tiêu đề CinePrime, không có lỗi hiển thị")
    public void publicPageOpens(String path) {
        driver.get(Config.baseUrl() + path);
        Assert.assertFalse(driver.getTitle().isBlank(), "Thiếu tiêu đề trang " + path);
        Assert.assertTrue(driver.findElements(By.cssSelector("header, .site-header, nav")).size() > 0, "Thiếu thanh điều hướng " + path);
        Assert.assertFalse(driver.getPageSource().contains("Whitelabel"), "Trang lỗi mặc định ở " + path);
    }

    @Test(groups = {"smoke", "public"}, description = "ID094 Trang chủ có phim đang chiếu (dữ liệu demo) và bấm vào mở được trang chi tiết")
    public void homeMovieOpensDetail() {
        driver.get(Config.baseUrl() + "/movies");
        var link = wait.until(d -> d.findElements(By.cssSelector("a[href^='/movies/']")).stream().findFirst().orElse(null));
        Assert.assertNotNull(link, "Phải có ít nhất một phim");
        String href = link.getDomAttribute("href");
        link.click();
        wait.until(d -> d.getCurrentUrl().contains(href));
        Assert.assertTrue(driver.getCurrentUrl().contains("/movies/"));
    }

    @Test(groups = {"regression", "public"}, description = "ID095 Chọn ghế khi chưa đăng nhập bị chuyển sang trang đăng nhập")
    public void bookingRequiresLogin() {
        driver.get(Config.baseUrl() + "/booking/1");
        wait.until(d -> d.getCurrentUrl().contains("/login"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test(groups = {"regression", "public"}, description = "ID096 Trang không tồn tại trả trang lỗi tiếng Việt của ứng dụng")
    public void unknownPathShowsFriendlyError() {
        driver.get(Config.baseUrl() + "/khong-co-trang-nay");
        Assert.assertFalse(driver.getPageSource().contains("Whitelabel"), "Không được lộ trang lỗi mặc định của Spring");
    }
}
