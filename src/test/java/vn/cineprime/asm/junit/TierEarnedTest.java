package vn.cineprime.asm.junit;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import vn.cineprime.domain.Domain.Tier;
import vn.cineprime.member.TierService;
import static org.junit.jupiter.api.Assertions.*;

/** Y3 - JUnit 5: hạng thành viên theo tổng chi tiêu (biên ngưỡng). */
@DisplayName("TierService.earned - hạng theo ngưỡng chi tiêu")
class TierEarnedTest {
    private final TierService service = new TierService(null, Clock.systemUTC(), null);
    private final List<Tier> tiers = new ArrayList<>();

    private void tier(String code, int rank, long min) { Tier t = new Tier(); t.code = code; t.rankOrder = rank; t.minSpend = min; tiers.add(t); }
    @BeforeEach void fixture() {
        tier("SILVER", 1, 0); tier("GOLD", 2, 2_000_000); tier("PLATINUM", 3, 5_000_000); tier("DIAMOND", 4, 10_000_000);
    }

    @ParameterizedTest(name = "ID031 Chi tiêu {0} VND -> {1}")
    @CsvSource({"0, SILVER", "1999999, SILVER", "2000000, GOLD", "4999999, GOLD", "5000000, PLATINUM", "10000000, DIAMOND", "999999999999, DIAMOND"})
    void earnedTierAtBoundaries(long spend, String expected) { assertEquals(expected, service.earned(tiers, spend).code); }

    @Test @DisplayName("ID032 Mã hạng cố định theo thứ tự SILVER < GOLD < PLATINUM < DIAMOND < VIP")
    void codesAreOrdered() { assertEquals(List.of("SILVER", "GOLD", "PLATINUM", "DIAMOND", "VIP"), TierService.CODES); }
}
