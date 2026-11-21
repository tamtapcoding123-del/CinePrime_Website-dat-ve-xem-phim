package vn.cineprime.asm.ui.pages;

import java.util.List;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import vn.cineprime.asm.ui.support.Config;

/**
 * Page Object: Admin > Thể loại (/admin/manage/genres) - màn CRUD dùng cho Y4.
 * Thêm/Sửa mở trong hộp thoại &lt;dialog&gt; (admin-dialog.js); "xóa" là Ngừng hoạt động (soft delete) qua hộp xác nhận (app.js).
 */
public class AdminGenresPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By addButton = By.cssSelector("a[data-list-add]");
    private final By dialogName = By.cssSelector("dialog[open] #dlg-name");
    private final By dialogSubmit = By.cssSelector("dialog[open] form.dialog-form button[type='submit']");
    private final By rows = By.cssSelector("table.management-table tbody tr");

    public AdminGenresPage(WebDriver driver, WebDriverWait wait) { this.driver = driver; this.wait = wait; }

    public AdminGenresPage open() { driver.get(Config.baseUrl() + "/admin/manage/genres"); wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.management-table, .empty"))); return this; }
    public AdminGenresPage search(String text) { driver.get(Config.baseUrl() + "/admin/manage/genres?q=" + java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8)); wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form.ops-filters"))); return this; }

    /** CREATE: mở hộp thoại Thêm mới, nhập tên, bấm Thêm mới và đợi trang tải lại có tên vừa thêm. */
    public void create(String name) {
        driver.findElement(addButton).click();
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(dialogName));
        field.clear(); field.sendKeys(name);
        driver.findElement(dialogSubmit).click();
        waitDialogClosed();
    }

    /** READ: có dòng nào trong bảng hiện đúng tên không. */
    public boolean hasRow(String name) { return !rowsContaining(name).isEmpty(); }
    public int rowCount() { return driver.findElements(rows).size(); }

    /** UPDATE: bấm Sửa ở dòng có tên cũ, đổi tên, lưu. */
    public void rename(String oldName, String newName) {
        rowsContaining(oldName).getFirst().findElement(By.cssSelector("a.action-btn.primary")).click();
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(dialogName));
        field.clear(); field.sendKeys(newName);
        driver.findElement(dialogSubmit).click();
        waitDialogClosed();
    }

    /** DELETE (ngừng hoạt động): menu ba chấm của dòng > Ngừng hoạt động > Xác nhận. */
    public void disable(String name) {
        WebElement row = rowsContaining(name).getFirst();
        row.findElement(By.cssSelector("details.row-menu summary")).click();
        row.findElement(By.cssSelector("button.menu-item")).click();
        WebElement confirm = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("dialog.cine-dialog[open] button:not(.secondary)")));
        confirm.click();
    }

    /** Trạng thái hiển thị của dòng (cột badge) - rỗng nếu không có dòng. */
    public String statusOf(String name) {
        List<WebElement> found = rowsContaining(name);
        if (found.isEmpty()) return "";
        List<WebElement> badge = found.getFirst().findElements(By.cssSelector(".status-badge"));
        return badge.isEmpty() ? "" : badge.getFirst().getText().trim();
    }

    public String dialogErrorText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("dialog[open] [data-dialog-error]:not([hidden]), dialog[open] .field-error:not([hidden])"))).getText();
    }
    public void submitDialogEmpty() {
        driver.findElement(addButton).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialogName));
        driver.findElement(dialogSubmit).click();
    }

    /** Đợi hộp thoại Thêm/Sửa đóng (đề xuất review R1: gom đoạn chờ lặp ở create và rename). */
    private void waitDialogClosed() {
        wait.until(d -> d.findElements(By.cssSelector("dialog[open]")).stream().noneMatch(WebElement::isDisplayed));
    }

    private List<WebElement> rowsContaining(String name) {
        return driver.findElements(rows).stream().filter(r -> r.getText().contains(name)).toList();
    }
}
