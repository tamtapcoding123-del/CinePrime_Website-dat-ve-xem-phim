package vn.cineprime.asm.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import vn.cineprime.asm.ui.support.Config;

/** Page Object: /login (auth.pug, mode=login). */
public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By email = By.cssSelector("form[action='/login'] input[name='email']");
    private final By password = By.cssSelector("form[action='/login'] input[name='password']");
    private final By submit = By.cssSelector("form[action='/login'] button[type='submit']");

    public LoginPage(WebDriver driver, WebDriverWait wait) { this.driver = driver; this.wait = wait; }

    public LoginPage open() { driver.get(Config.baseUrl() + "/login"); wait.until(ExpectedConditions.visibilityOfElementLocated(email)); return this; }
    public LoginPage typeEmail(String value) { WebElement e = driver.findElement(email); e.clear(); e.sendKeys(value); return this; }
    public LoginPage typePassword(String value) { WebElement e = driver.findElement(password); e.clear(); e.sendKeys(value); return this; }
    public void submit() { driver.findElement(submit).click(); }

    /** Đăng nhập trọn bước; trả về sau khi trình duyệt rời trang /login hoặc quay lại /login?error. */
    public void loginAs(String mail, String pass) {
        open().typeEmail(mail).typePassword(pass).submit();
        wait.until(d -> !d.getCurrentUrl().endsWith("/login") || d.getCurrentUrl().contains("?error"));
    }
    public boolean isShowingError() { return driver.getCurrentUrl().contains("/login?error"); }
}
