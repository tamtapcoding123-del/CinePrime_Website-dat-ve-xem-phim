package vn.cineprime.asm.junit;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import vn.cineprime.admin.ShowtimeRules;
import vn.cineprime.domain.Domain.*;
import static org.junit.jupiter.api.Assertions.*;

/** Y3 - JUnit 5: quy tắc xếp lịch chiếu (đệm dọn phòng 15 phút) và điều kiện mở bán. */
@DisplayName("ShowtimeRules - chồng giờ và điều kiện mở bán")
class ShowtimeRulesTest {
    static final Instant T10 = Instant.parse("2026-11-20T03:00:00Z"); // 10:00 VN thứ Sáu
    Movie movie = new Movie(); Room room = new Room(); Cinema cinema = new Cinema();
    List<Seat> seats = new ArrayList<>(); List<Price> prices = new ArrayList<>();

    @BeforeEach void fixture() {
        movie.active = true; room.active = true; cinema.active = true;
        Seat s = new Seat(); s.type = "STANDARD"; seats.add(s);
        Price p = new Price(); p.seatType = "STANDARD"; p.dayType = "WEEKDAY"; p.fromMinute = 0; p.toMinute = 1440; p.amount = 85000; prices.add(p);
    }
    List<String> errors(Instant start, long surcharge, boolean visible) { return ShowtimeRules.openingErrors(movie, room, cinema, seats, prices, visible, start, surcharge); }

    @Test @DisplayName("ID020 Suất bắt đầu đúng lúc suất trước kết thúc + 15 phút dọn phòng là hợp lệ")
    void exactCleanupGapAllowed() {
        assertFalse(ShowtimeRules.overlaps(T10.plusSeconds(105 * 60 + 900), T10.plusSeconds(200 * 60), T10, T10.plusSeconds(105 * 60)));
    }
    @Test @DisplayName("ID021 Lệch 1 giây so với mốc đệm 15 phút là chồng giờ")
    void oneSecondInsideCleanupOverlaps() {
        assertTrue(ShowtimeRules.overlaps(T10.plusSeconds(105 * 60 + 899), T10.plusSeconds(200 * 60), T10, T10.plusSeconds(105 * 60)));
    }
    @Test @DisplayName("ID022 Chồng giờ đối xứng (A đè B và B đè A)")
    void overlapIsSymmetric() {
        Instant a1 = T10, a2 = T10.plusSeconds(7200), b1 = T10.plusSeconds(3600), b2 = T10.plusSeconds(10800);
        assertEquals(ShowtimeRules.overlaps(a1, a2, b1, b2), ShowtimeRules.overlaps(b1, b2, a1, a2));
        assertTrue(ShowtimeRules.overlaps(a1, a2, b1, b2));
    }
    @Test @DisplayName("ID023 Dữ liệu hợp lệ không có lỗi mở bán")
    void validShowHasNoErrors() { assertEquals(List.of(), errors(T10, 0, true)); }
    @Test @DisplayName("ID024 Phim/rạp/phòng ngừng hoạt động chặn mở bán")
    void inactiveParentsBlock() {
        movie.active = false;
        assertTrue(errors(T10, 0, true).contains("Phim/rạp/phòng đã ngừng hoạt động."));
    }
    @Test @DisplayName("ID025 Phim chưa có thể loại hoạt động chặn mở bán")
    void movieWithoutActiveGenreBlocks() { assertTrue(errors(T10, 0, false).contains("Phim chưa có thể loại hoạt động.")); }
    @Test @DisplayName("ID026 Phòng không có ghế hoạt động chặn mở bán")
    void roomWithoutSeatsBlocks() { seats.getFirst().active = false; assertTrue(errors(T10, 0, true).contains("Phòng chưa có ghế hoạt động.")); }
    @Test @DisplayName("ID027 Ghế đôi lẻ (nhóm chỉ có 1 ghế) chặn mở bán")
    void coupleGroupMustHaveExactlyTwoSeats() {
        Seat c = new Seat(); c.type = "COUPLE"; c.coupleGroup = "F-1"; seats.add(c);
        assertTrue(errors(T10, 0, true).contains("Mỗi nhóm ghế đôi phải có đúng 2 ghế hoạt động."));
    }
    @Test @DisplayName("ID028 Thiếu giá cho loại ghế của phòng chặn mở bán")
    void missingPriceBlocks() { prices.clear(); assertTrue(errors(T10, 0, true).stream().anyMatch(e -> e.startsWith("Thiếu hoặc trùng giá STANDARD"))); }
    @Test @DisplayName("ID029 Phụ thu làm giá vượt giới hạn số tiền bị báo lỗi")
    void surchargeOverflowReported() {
        prices.getFirst().amount = Long.MAX_VALUE;
        assertTrue(errors(T10, 1, true).contains("Giá vé cộng phụ thu vượt giới hạn số tiền."));
    }
    @Test @DisplayName("ID030 Chỉ có giá ngày thường: suất thứ Bảy bị chặn đúng loại ngày")
    void weekendUsesWeekendPrice() {
        Instant saturday = Instant.parse("2026-11-21T03:00:00Z");
        assertTrue(errors(saturday, 0, true).stream().anyMatch(e -> e.startsWith("Thiếu hoặc trùng giá")));
    }
}
