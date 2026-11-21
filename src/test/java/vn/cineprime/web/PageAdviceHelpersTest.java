package vn.cineprime.web;

import java.time.LocalDate;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/** Y3 - JUnit 5: hàm hiển thị dùng chung của PageAdvice (chữ viết tắt avatar, tên thứ trong tuần). */
class PageAdviceHelpersTest {
    @Test @DisplayName("ID055 Avatar lấy chữ cái đầu của từ đầu và từ cuối, viết hoa")
    void initialsFirstAndLast() { assertEquals("NA", PageAdvice.initials("Nguyễn Văn An")); assertEquals("TM", PageAdvice.initials("  trần   thị  mai ")); }
    @Test @DisplayName("ID056 Tên một từ chỉ có một chữ; rỗng hoặc null là dấu ?")
    void initialsEdgeCases() { assertEquals("A", PageAdvice.initials("An")); assertEquals("?", PageAdvice.initials("")); assertEquals("?", PageAdvice.initials(null)); }
    @Test @DisplayName("ID057 Chữ Đ/Ê tiếng Việt được viết hoa đúng")
    void vietnameseUppercase() { assertEquals("ĐE", PageAdvice.initials("đào em")); }
    @Test @DisplayName("ID058 Tên thứ: Thứ 2..Thứ 7, Chủ nhật")
    void weekdayNames() {
        assertEquals("Thứ 2", PageAdvice.weekday(LocalDate.of(2026, 11, 16)));
        assertEquals("Thứ 4", PageAdvice.weekday(LocalDate.of(2026, 11, 18)));
        assertEquals("Thứ 7", PageAdvice.weekday(LocalDate.of(2026, 11, 21)));
        assertEquals("Chủ nhật", PageAdvice.weekday(LocalDate.of(2026, 11, 22)));
    }
}
