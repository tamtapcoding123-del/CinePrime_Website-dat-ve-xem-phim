package vn.cineprime.asm.ui.tests;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.*;
import vn.cineprime.asm.ui.pages.*;
import vn.cineprime.asm.ui.support.*;

/**
 * Y4 - Selenium + TestNG: chức năng Đăng ký (/register).
 * Giới hạn của ứng dụng: tối đa 5 lần POST /register mỗi 5 phút trên một IP, nên chỉ ca cần server mới gửi form thật.
 * Ca kiểm tra HTML5 (không gửi form) không tốn lượt.
 */
public class RegisterUiTest extends BaseUiTest {

    @Test(groups = {"smoke", "register"}, description = "ID076 Trang đăng ký hiển thị đủ 3 ô và nút Tạo tài khoản")
    public void registerPageShowsForm() {
        RegisterPage page = new RegisterPage(driver, wait).open();
        Assert.assertEquals(driver.findElements(By.cssSelector("form[action='/register'] input")).stream().filter(e -> !"hidden".equals(e.getDomAttribute("type"))).count(), 3);
        Assert.assertEquals(page.passwordMinLength(), "10");
        Assert.assertTrue(driver.findElement(By.cssSelector("form[action='/register'] button[type='submit']")).getText().contains("Tạo tài khoản"));
    }

    @Test(groups = {"smoke", "register"}, description = "ID077 Đăng ký hợp lệ chuyển sang /login?registered rồi đăng nhập được bằng tài khoản mới")
    public void registerValidThenLogin() {
        String email = uniqueEmail("sel");
        new RegisterPage(driver, wait).open().fill("Sinh Viên Selenium", email, "MatKhau-Selenium-1").submit();
        wait.until(d -> d.getCurrentUrl().contains("/login?registered"));
        new LoginPage(driver, wait).loginAs(email, "MatKhau-Selenium-1");
        Assert.assertTrue(driver.getCurrentUrl().endsWith("/account"), "Sau đăng nhập khách phải vào /account, hiện tại: " + driver.getCurrentUrl());
    }

    @DataProvider(name = "invalidClientSide")
    public Object[][] invalidClientSide() {
        return new Object[][]{
            {"Họ tên rỗng", "", "a@cineprime.test", "MatKhau-Selenium-1", "name"},
            {"Email rỗng", "An", "", "MatKhau-Selenium-1", "email"},
            {"Email thiếu @", "An", "khong-phai-email", "MatKhau-Selenium-1", "email"},
            {"Mật khẩu 9 ký tự", "An", "a@cineprime.test", "123456789", "password"},
            {"Mật khẩu rỗng", "An", "a@cineprime.test", "", "password"},
        };
    }

    @Test(groups = {"regression", "register"}, dataProvider = "invalidClientSide", description = "ID078 Trình duyệt chặn dữ liệu sai trước khi gửi server")
    public void browserBlocksInvalidInput(String label, String name, String email, String password, String badField) {
        RegisterPage page = new RegisterPage(driver, wait).open().fill(name, email, password);
        Assert.assertFalse(page.isFormValid(), label + ": form phải không hợp lệ");
        Assert.assertFalse(page.validationMessage(badField).isEmpty(), label + ": ô " + badField + " phải có thông báo kiểm tra");
        page.submit();
        Assert.assertTrue(driver.getCurrentUrl().endsWith("/register"), label + ": không được rời trang đăng ký");
    }

    @Test(groups = {"regression", "register"}, description = "ID079 Email 'a@b' qua được kiểm tra HTML5 nhưng server từ chối (kiểm tra hai lớp)")
    public void serverRejectsEmailWithoutDomainDot() {
        new RegisterPage(driver, wait).open().fill("An", "a@b", "MatKhau-Selenium-1").submit();
        wait.until(d -> d.getPageSource().contains("Kiểm tra email và họ tên"));
        Assert.assertTrue(driver.getPageSource().contains("Kiểm tra email và họ tên"));
    }

    @Test(groups = {"regression", "register"}, description = "ID080 Đăng ký trùng email (khác hoa thường) bị từ chối bằng thông báo chung")
    public void duplicateEmailRejected() {
        String email = uniqueEmail("dup");
        new RegisterPage(driver, wait).open().fill("Người A", email, "MatKhau-Selenium-1").submit();
        wait.until(d -> d.getCurrentUrl().contains("/login?registered"));
        new RegisterPage(driver, wait).open().fill("Người B", email.toUpperCase(), "MatKhau-Selenium-1").submit();
        wait.until(d -> d.getPageSource().contains("Không thể đăng ký email này"));
        Assert.assertFalse(driver.getPageSource().toLowerCase().contains("đã tồn tại"), "Không được tiết lộ email đã tồn tại");
    }
}
