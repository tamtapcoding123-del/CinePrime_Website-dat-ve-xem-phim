package vn.cineprime.asm.ui.tests;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;
import vn.cineprime.asm.ui.pages.LoginPage;
import vn.cineprime.asm.ui.support.*;

/** Y4 - Selenium + TestNG: chức năng Đăng nhập và phân quyền. Cần ứng dụng chạy với DEMO_SEED=true và -Ddemo.password. */
public class LoginUiTest extends BaseUiTest {

    private void requireDemoPassword() {
        if (Config.demoPassword().isBlank()) throw new SkipException("Chưa có -Ddemo.password (DEMO_PASSWORD của ứng dụng) nên bỏ qua ca cần tài khoản demo");
    }

    @Test(groups = {"smoke", "login"}, description = "ID081 Admin đăng nhập đúng được đưa vào /admin")
    public void adminLoginGoesToAdmin() {
        requireDemoPassword();
        new LoginPage(driver, wait).loginAs(Config.adminEmail(), Config.demoPassword());
        wait.until(d -> d.getCurrentUrl().contains("/admin"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin"));
    }

    @Test(groups = {"smoke", "login"}, description = "ID082 Khách đăng nhập đúng được đưa vào /account")
    public void customerLoginGoesToAccount() {
        requireDemoPassword();
        new LoginPage(driver, wait).loginAs(Config.customerEmail(), Config.demoPassword());
        Assert.assertTrue(driver.getCurrentUrl().endsWith("/account"), driver.getCurrentUrl());
    }

    @Test(groups = {"regression", "login"}, description = "ID083 Sai mật khẩu quay lại /login?error")
    public void wrongPasswordShowsError() {
        LoginPage page = new LoginPage(driver, wait);
        page.loginAs(Config.customerEmail(), "mat-khau-sai-123");
        Assert.assertTrue(page.isShowingError(), driver.getCurrentUrl());
    }

    @Test(groups = {"regression", "login"}, description = "ID084 Email không tồn tại cũng chỉ báo lỗi chung")
    public void unknownEmailShowsSameError() {
        LoginPage page = new LoginPage(driver, wait);
        page.loginAs("khong-ton-tai@cineprime.test", "mat-khau-sai-123");
        Assert.assertTrue(page.isShowingError(), driver.getCurrentUrl());
    }

    @Test(groups = {"smoke", "login"}, description = "ID085 Chưa đăng nhập mà vào /admin bị đưa về /login")
    public void adminRequiresLogin() {
        driver.get(Config.baseUrl() + "/admin");
        wait.until(d -> d.getCurrentUrl().contains("/login"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test(groups = {"regression", "login"}, description = "ID086 Khách hàng đăng nhập rồi mở /admin bị từ chối (403), không thấy trang quản trị")
    public void customerCannotOpenAdmin() {
        requireDemoPassword();
        new LoginPage(driver, wait).loginAs(Config.customerEmail(), Config.demoPassword());
        driver.get(Config.baseUrl() + "/admin");
        Assert.assertFalse(driver.getCurrentUrl().endsWith("/admin/"), "không được ở trang admin");
        Assert.assertTrue(driver.getPageSource().contains("403") || driver.getPageSource().contains("Forbidden") || driver.findElements(By.cssSelector(".admin-shell")).isEmpty(),
            "Khách không được thấy khung quản trị");
    }

    @Test(groups = {"regression", "login"}, description = "ID087 Đăng xuất xong vào lại /account phải đăng nhập lại")
    public void logoutEndsSession() {
        requireDemoPassword();
        new LoginPage(driver, wait).loginAs(Config.customerEmail(), Config.demoPassword());
        String csrf = (String) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("var m=document.querySelector('meta[name=csrf-token]');return m?m.content:'';");
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("var f=document.createElement('form');f.method='post';f.action='/logout';var i=document.createElement('input');i.type='hidden';i.name='_csrf';i.value=arguments[0];f.appendChild(i);document.body.appendChild(f);f.submit();", csrf);
        wait.until(d -> d.getCurrentUrl().contains("/login"));
        driver.get(Config.baseUrl() + "/account");
        wait.until(d -> d.getCurrentUrl().contains("/login"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"));
    }
}
