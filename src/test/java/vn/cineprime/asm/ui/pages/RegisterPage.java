package vn.cineprime.asm.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import vn.cineprime.asm.ui.support.Config;

/** Page Object: /register (auth.pug, mode=register). */
public class RegisterPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By name = By.cssSelector("form[action='/register'] input[name='name']");
    private final By email = By.cssSelector("form[action='/register'] input[name='email']");
    private final By password = By.cssSelector("form[action='/register'] input[name='password']");
    private final By submit = By.cssSelector("form[action='/register'] button[type='submit']");

    public RegisterPage(WebDriver driver, WebDriverWait wait) { this.driver = driver; this.wait = wait; }

    public RegisterPage open() { driver.get(Config.baseUrl() + "/register"); wait.until(ExpectedConditions.visibilityOfElementLocated(name)); return this; }
    public RegisterPage fill(String fullName, String mail, String pass) {
        set(name, fullName); set(email, mail); set(password, pass); return this;
    }
    public void submit() { driver.findElement(submit).click(); }

    /** Thông báo kiểm tra của chính trình duyệt (HTML5 validation) cho một ô nhập: rỗng nghĩa là hợp lệ. */
    public String validationMessage(String field) {
        return (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", driver.findElement(By.cssSelector("form[action='/register'] input[name='" + field + "']")));
    }
    public boolean isFormValid() { return (Boolean) ((JavascriptExecutor) driver).executeScript("return document.querySelector(\"form[action='/register']\").checkValidity();"); }
    public String passwordMinLength() { return driver.findElement(password).getDomAttribute("minlength"); }

    private void set(By by, String value) { WebElement e = driver.findElement(by); e.clear(); e.sendKeys(value); }
}
