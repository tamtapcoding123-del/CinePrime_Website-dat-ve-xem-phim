package vn.cineprime.asm.junit;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.invocation.InvocationOnMock;
import vn.cineprime.booking.BookingService;
import vn.cineprime.domain.Domain.*;
import vn.cineprime.domain.Store;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Y3 - JUnit 5: quy tắc giá vé BookingService.price (rạp x loại ghế x ngày thường/cuối tuần x khung giờ + phụ thu). Tiền là số nguyên VND. */
@DisplayName("BookingService.price - bảng giá và múi giờ Asia/Ho_Chi_Minh")
class BookingPriceTest {
    private final List<Price> prices = new ArrayList<>();
    private BookingService booking;
    private final Room room = new Room();

    private static Price price(String seat, String day, int from, int to, long amount) {
        Price p = new Price(); p.cinemaId = 1L; p.seatType = seat; p.dayType = day; p.fromMinute = from; p.toMinute = to; p.amount = amount; return p;
    }

    @BeforeEach void setUp() {
        room.id = 7L; room.cinemaId = 1L;
        // Store giả: get(Room) trả phòng cố định; list(Price, where, args...) lọc theo cinemaId, loại ghế, loại ngày, phút trong ngày.
        Store db = mock(Store.class, (InvocationOnMock call) -> {
            Object[] a = call.getArguments();
            if (call.getMethod().getName().equals("get") && a[0] == Room.class) return room;
            if (call.getMethod().getName().equals("list") && a[0] == Price.class) {
                return prices.stream().filter(p -> p.cinemaId.equals(a[2]) && p.seatType.equals(a[3]) && p.dayType.equals(a[4]) && p.fromMinute <= (int) a[5] && p.toMinute > (int) a[5]).toList();
            }
            return RETURNS_DEFAULTS.answer(call);
        });
        for (String type : List.of("STANDARD", "VIP", "COUPLE"))
            for (String day : List.of("WEEKDAY", "WEEKEND")) {
                long base = (type.equals("VIP") ? 105000 : type.equals("COUPLE") ? 95000 : 85000) + (day.equals("WEEKEND") ? 10000 : 0);
                prices.add(price(type, day, 0, 1020, base));
                prices.add(price(type, day, 1020, 1440, base + 5000));
            }
        booking = new BookingService(db, Clock.systemUTC(), null, null, null);
    }

    private Showtime showAt(String utcInstant, long surcharge) {
        Showtime s = new Showtime(); s.roomId = 7L; s.startsAt = Instant.parse(utcInstant); s.surcharge = surcharge; return s;
    }
    private Seat seat(String type) { Seat s = new Seat(); s.type = type; return s; }

    @ParameterizedTest(name = "ID014 {0} lúc {1} (UTC) -> {2} VND")
    @CsvSource({
        "STANDARD, 2026-11-18T03:00:00Z, 85000",
        "VIP,      2026-11-18T03:00:00Z, 105000",
        "COUPLE,   2026-11-18T03:00:00Z, 95000",
        "STANDARD, 2026-11-18T09:59:00Z, 85000",
        "STANDARD, 2026-11-18T10:00:00Z, 90000",
        "STANDARD, 2026-11-21T03:00:00Z, 95000",
        "VIP,      2026-11-22T11:00:00Z, 120000"
    })
    void priceFollowsSeatTypeDayAndTimeBand(String type, String start, long expected) {
        assertEquals(expected, booking.price(showAt(start, 0), seat(type)));
    }

    @Test @DisplayName("ID015 Tối thứ Sáu 23:00 UTC đã là sáng thứ Bảy 06:00 giờ Việt Nam -> tính giá cuối tuần")
    void weekendIsDecidedInVietnamTimeNotUtc() {
        assertEquals(95000, booking.price(showAt("2026-11-20T23:00:00Z", 0), seat("STANDARD")));
    }

    @Test @DisplayName("ID016 Phụ thu suất chiếu được cộng nguyên vào giá")
    void surchargeIsAdded() {
        assertEquals(85000 + 20000, booking.price(showAt("2026-11-18T03:00:00Z", 20000), seat("STANDARD")));
    }

    @Test @DisplayName("ID017 Thiếu khung giá thì chặn bán, không đoán giá")
    void missingPriceBlocksSale() {
        prices.removeIf(p -> p.seatType.equals("VIP"));
        var e = assertThrows(IllegalArgumentException.class, () -> booking.price(showAt("2026-11-18T03:00:00Z", 0), seat("VIP")));
        assertEquals("Bảng giá chưa đầy đủ hoặc trùng khung giờ.", e.getMessage());
    }

    @Test @DisplayName("ID018 Hai khung giá chồng nhau bị coi là cấu hình sai")
    void overlappingPricesRejected() {
        prices.add(price("STANDARD", "WEEKDAY", 0, 1440, 70000));
        assertThrows(IllegalArgumentException.class, () -> booking.price(showAt("2026-11-18T03:00:00Z", 0), seat("STANDARD")));
    }

    @Test @DisplayName("ID019 Giá cộng phụ thu tràn số nguyên long ném ArithmeticException (không âm thầm đổi dấu)")
    void overflowIsNotSilent() {
        prices.clear(); prices.add(price("STANDARD", "WEEKDAY", 0, 1440, Long.MAX_VALUE));
        assertThrows(ArithmeticException.class, () -> booking.price(showAt("2026-11-18T03:00:00Z", 1), seat("STANDARD")));
    }
}
