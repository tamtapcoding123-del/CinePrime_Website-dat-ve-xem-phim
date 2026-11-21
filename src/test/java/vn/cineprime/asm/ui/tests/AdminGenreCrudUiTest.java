package vn.cineprime.asm.ui.tests;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;
import vn.cineprime.asm.ui.pages.*;
import vn.cineprime.asm.ui.support.*;

/**
 * Y4 - Selenium + TestNG: CRUD trên trang Admin > Thể loại. Các bước phụ thuộc nhau (Create -> Read -> Update -> Delete)
 * nên dùng dependsOnMethods và một tên thể loại duy nhất cho mỗi lần chạy.
 */
public class AdminGenreCrudUiTest extends BaseUiTest {
    private static final String RUN = Long.toString(System.currentTimeMillis() % 100000, 36);
    private static final String NAME = "Kiểm thử " + RUN;
    private static final String RENAMED = "Kiểm thử đổi tên " + RUN;

    private AdminGenresPage adminPage() {
        if (Config.demoPassword().isBlank()) throw new SkipException("Cần -Ddemo.password để đăng nhập Admin");
        new LoginPage(driver, wait).loginAs(Config.adminEmail(), Config.demoPassword());
        wait.until(d -> d.getCurrentUrl().contains("/admin"));
        return new AdminGenresPage(driver, wait).open();
    }

    @Test(groups = {"regression", "crud"}, description = "ID088 Create: thêm thể loại mới và thấy ngay trong danh sách")
    public void createGenre() {
        AdminGenresPage page = adminPage();
        page.create(NAME);
        page.search(NAME);
        Assert.assertTrue(page.hasRow(NAME), "Thể loại mới phải hiện trong danh sách");
    }

    @Test(groups = {"regression", "crud"}, dependsOnMethods = "createGenre", description = "ID089 Read: tìm theo từ khoá chỉ ra đúng 1 dòng; từ khoá lạ không ra dòng nào")
    public void readGenreBySearch() {
        AdminGenresPage page = adminPage();
        page.search(NAME);
        Assert.assertEquals(page.rowCount(), 1);
        page.search("khong-co-the-loai-nay-" + RUN);
        Assert.assertFalse(page.hasRow(NAME));
    }

    @Test(groups = {"regression", "crud"}, dependsOnMethods = "createGenre", description = "ID090 Create: tên rỗng bị chặn, hộp thoại giữ nguyên")
    public void createWithEmptyNameBlocked() {
        AdminGenresPage page = adminPage();
        page.submitDialogEmpty();
        Assert.assertFalse(page.dialogErrorText().isBlank(), "Phải có thông báo lỗi khi tên rỗng");
    }

    @Test(groups = {"regression", "crud"}, dependsOnMethods = "readGenreBySearch", description = "ID091 Update: đổi tên thể loại, tên cũ biến mất, tên mới xuất hiện")
    public void updateGenre() {
        AdminGenresPage page = adminPage();
        page.search(NAME);
        page.rename(NAME, RENAMED);
        page.search(RENAMED);
        Assert.assertTrue(page.hasRow(RENAMED));
        page.search(NAME);
        Assert.assertFalse(page.hasRow(NAME), "Tên cũ không được còn trong danh sách");
    }

    @Test(groups = {"regression", "crud"}, dependsOnMethods = "updateGenre", description = "ID092 Delete (ngừng hoạt động): sau khi xác nhận trạng thái đổi sang ngừng")
    public void disableGenre() {
        AdminGenresPage page = adminPage();
        page.search(RENAMED);
        String before = page.statusOf(RENAMED);
        page.disable(RENAMED);
        page.search(RENAMED);
        String after = page.statusOf(RENAMED);
        Assert.assertNotEquals(after, before, "Trạng thái phải đổi sau khi ngừng hoạt động (trước: " + before + ", sau: " + after + ")");
    }
}
