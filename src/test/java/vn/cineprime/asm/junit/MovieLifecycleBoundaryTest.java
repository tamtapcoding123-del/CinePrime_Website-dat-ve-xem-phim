package vn.cineprime.asm.junit;

import java.time.*;
import org.junit.jupiter.api.*;
import vn.cineprime.domain.Domain.Movie;
import vn.cineprime.domain.MovieLifecycle;
import static org.junit.jupiter.api.Assertions.*;

/** Y3 - JUnit 5: bổ sung ca biên cho vòng đời phim (khung phát hành, nhãn, suất ngoài khung). */
@DisplayName("MovieLifecycle - ca biên bổ sung")
class MovieLifecycleBoundaryTest {
    static Movie movie(LocalDate r, LocalDate e) { Movie m = new Movie(); m.releaseDate = r; m.endDate = e; m.active = true; return m; }
    static final ZoneId VN = MovieLifecycle.ZONE;

    @Test @DisplayName("ID033 Phim chưa có ngày phát hành luôn là Sắp chiếu")
    void noReleaseDateIsComingSoon() { assertEquals("COMING_SOON", MovieLifecycle.status(movie(null, null), LocalDate.of(2026, 11, 18))); }
    @Test @DisplayName("ID034 Ngày khởi chiếu là ngày đầu tiên Đang chiếu")
    void releaseDayIsNowShowing() { assertEquals("NOW_SHOWING", MovieLifecycle.status(movie(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 12, 18)), LocalDate.of(2026, 11, 18))); }
    @Test @DisplayName("ID035 Nhãn tiếng Việt cho từng trạng thái, trạng thái lạ trả dấu gạch")
    void labels() {
        assertEquals("Sắp chiếu", MovieLifecycle.label("COMING_SOON"));
        assertEquals("Đang chiếu", MovieLifecycle.label("NOW_SHOWING"));
        assertEquals("Đã kết thúc", MovieLifecycle.label("ENDED"));
        assertEquals("—", MovieLifecycle.label("XYZ"));
    }
    @Test @DisplayName("ID036 Suất chiếu nằm trong khung phát hành; ngày cuối vẫn hợp lệ (giờ Việt Nam)")
    void showtimeInsideWindow() {
        Movie m = movie(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20));
        assertTrue(MovieLifecycle.inWindow(m, LocalDate.of(2026, 11, 20).atTime(23, 30).atZone(VN).toInstant()));
        assertFalse(MovieLifecycle.inWindow(m, LocalDate.of(2026, 11, 21).atStartOfDay(VN).toInstant()));
        assertFalse(MovieLifecycle.inWindow(m, LocalDate.of(2026, 11, 9).atTime(23, 59).atZone(VN).toInstant()));
    }
    @Test @DisplayName("ID037 17:30 UTC hôm trước đã là ngày hôm sau theo giờ Việt Nam")
    void utcEveningIsNextVietnamDay() { assertEquals(LocalDate.of(2026, 11, 21), MovieLifecycle.day(Instant.parse("2026-11-20T17:30:00Z"))); }
    @Test @DisplayName("ID038 Bán vé trước ngày phát hành được phép, sau ngày kết thúc thì dừng")
    void advanceSalesAllowedButNotAfterEnd() {
        Movie m = movie(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));
        assertFalse(MovieLifecycle.afterEnd(m, LocalDate.of(2026, 11, 25).atTime(10, 0).atZone(VN).toInstant()));
        assertTrue(MovieLifecycle.afterEnd(m, LocalDate.of(2027, 1, 1).atTime(10, 0).atZone(VN).toInstant()));
    }
    @Test @DisplayName("ID039 Thông báo lỗi khung phát hành ghi rõ dd/MM/yyyy; null khi hợp lệ")
    void windowErrorText() {
        Movie m = movie(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20));
        assertNull(MovieLifecycle.windowError(m, LocalDate.of(2026, 11, 15).atTime(10, 0).atZone(VN).toInstant()));
        assertEquals("Ngày chiếu phải nằm trong thời gian phát hành của phim (10/11/2026 – 20/11/2026).", MovieLifecycle.windowError(m, LocalDate.of(2026, 12, 1).atTime(10, 0).atZone(VN).toInstant()));
    }
    @Test @DisplayName("ID040 Phim ẩn (active=false) không hiển thị dù còn trong khung phát hành")
    void hiddenMovieNotListed() { Movie m = movie(LocalDate.of(2026, 11, 1), null); m.active = false; assertFalse(MovieLifecycle.listed(m, LocalDate.of(2026, 11, 18))); }
}
