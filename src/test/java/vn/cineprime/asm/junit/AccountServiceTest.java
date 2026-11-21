package vn.cineprime.asm.junit;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import vn.cineprime.auth.AccountService;
import vn.cineprime.domain.Domain.User;
import vn.cineprime.domain.Store;
import vn.cineprime.notification.Notifications;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Y3 - JUnit 5 + Mockito: nghiệp vụ Đăng ký / Quên mật khẩu của AccountService (AccountController gọi trực tiếp). */
@DisplayName("AccountService - đăng ký, mật khẩu, token")
class AccountServiceTest {
    private Store db;
    private Notifications notices;
    private AccountService service;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @BeforeEach void setUp() {
        db = mock(Store.class);
        notices = mock(Notifications.class);
        when(db.first(any(), anyString(), any(Object[].class))).thenReturn(Optional.empty());
        when(db.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        service = new AccountService(db, encoder, Clock.fixed(Instant.parse("2026-11-18T03:00:00Z"), ZoneOffset.UTC), notices);
    }

    @Test @DisplayName("ID001 Mật khẩu hợp lệ 10 ký tự được chấp nhận")
    void passwordTenCharactersIsAccepted() { assertDoesNotThrow(() -> service.passwordValid("abcdefghij")); }

    @ParameterizedTest(name = "ID002 Mật khẩu \"{0}\" bị từ chối")
    @NullSource @ValueSource(strings = {"", "123456789"})
    void shortOrNullPasswordIsRejected(String raw) {
        var e = assertThrows(IllegalArgumentException.class, () -> service.passwordValid(raw));
        assertTrue(e.getMessage().contains("ít nhất 10 ký tự"));
    }

    @Test @DisplayName("ID003 Mật khẩu 72 byte được chấp nhận, 73 byte bị từ chối (BCrypt giới hạn theo byte, không theo ký tự)")
    void passwordByteBoundary() {
        assertDoesNotThrow(() -> service.passwordValid("a".repeat(72)));
        assertThrows(IllegalArgumentException.class, () -> service.passwordValid("a".repeat(73)));
        assertThrows(IllegalArgumentException.class, () -> service.passwordValid("ế".repeat(30)), "30 ký tự tiếng Việt = 90 byte UTF-8");
    }

    @ParameterizedTest(name = "ID004 Email sai định dạng \"{0}\" bị từ chối")
    @ValueSource(strings = {"khongcoat", "a@b", "a b@c.vn", "@cineprime.vn", "ten@"})
    void invalidEmailRejected(String email) {
        var e = assertThrows(IllegalArgumentException.class, () -> service.register(email, "Nguyễn An", "matkhau12345"));
        assertEquals("Kiểm tra email và họ tên.", e.getMessage());
        verify(db, never()).save(any(User.class));
    }

    @Test @DisplayName("ID005 Họ tên rỗng hoặc dài hơn 100 ký tự bị từ chối")
    void invalidNameRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.register("an@cineprime.vn", "   ", "matkhau12345"));
        assertThrows(IllegalArgumentException.class, () -> service.register("an@cineprime.vn", "A".repeat(101), "matkhau12345"));
        assertDoesNotThrow(() -> service.register("an@cineprime.vn", "A".repeat(100), "matkhau12345"));
    }

    @Test @DisplayName("ID006 Email dài hơn 254 ký tự bị từ chối")
    void emailTooLongRejected() {
        String longEmail = "a".repeat(250) + "@x.vn";
        assertThrows(IllegalArgumentException.class, () -> service.register(longEmail, "An", "matkhau12345"));
    }

    @Test @DisplayName("ID007 Email đã tồn tại: thông báo chung, không lộ tài khoản có hay không")
    void duplicateEmailUsesGenericMessage() {
        when(db.first(eq(User.class), eq("where e.email=?1"), eq("trung@cineprime.vn"))).thenReturn(Optional.of(new User()));
        var e = assertThrows(IllegalArgumentException.class, () -> service.register("trung@cineprime.vn", "An", "matkhau12345"));
        assertTrue(e.getMessage().startsWith("Không thể đăng ký email này"));
    }

    @Test @DisplayName("ID008 Đăng ký thành công: email chuẩn hoá chữ thường, tên cắt khoảng trắng, mật khẩu băm BCrypt, role CUSTOMER")
    void registerNormalisesAndHashes() {
        User u = service.register("  AN.Nguyen@CinePrime.VN ", "  Nguyễn Văn An ", "matkhau12345");
        assertEquals("an.nguyen@cineprime.vn", u.email);
        assertEquals("Nguyễn Văn An", u.name);
        assertNotEquals("matkhau12345", u.passwordHash);
        assertTrue(encoder.matches("matkhau12345", u.passwordHash));
        assertEquals("CUSTOMER", u.role, "Tài khoản tự đăng ký luôn là CUSTOMER, không tự cấp quyền");
    }

    @Test @DisplayName("ID009 hash() là SHA-256 hex 64 ký tự, cố định theo đầu vào")
    void tokenHashIsDeterministicSha256() {
        assertEquals(AccountService.hash("abc"), AccountService.hash("abc"));
        assertEquals(64, AccountService.hash("abc").length());
        assertNotEquals(AccountService.hash("abc"), AccountService.hash("abd"));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", AccountService.hash("abc"));
    }

    @Test @DisplayName("ID010 token() sinh 43 ký tự URL-safe, không trùng trong 1000 lần")
    void tokensAreLongAndUnique() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String t = AccountService.token();
            assertEquals(43, t.length());
            assertTrue(t.matches("[A-Za-z0-9_-]+"));
            assertTrue(seen.add(t), "token trùng");
        }
    }

    @Test @DisplayName("ID011 Quên mật khẩu với email không tồn tại: không lỗi, không gửi mail (chống dò email)")
    void forgotUnknownEmailIsSilent() {
        assertDoesNotThrow(() -> service.forgot("khongco@cineprime.vn"));
        verifyNoInteractions(notices);
    }

    @Test @DisplayName("ID012 Đặt lại mật khẩu với token lạ bị từ chối")
    void resetWithUnknownTokenRejected() {
        var e = assertThrows(IllegalArgumentException.class, () -> service.reset("token-khong-ton-tai", "matkhaumoi123"));
        assertEquals("Liên kết không hợp lệ hoặc đã hết hạn.", e.getMessage());
    }

    @Test @DisplayName("ID013 Đặt lại mật khẩu: mật khẩu mới quá ngắn bị chặn trước khi xét token")
    void resetWithShortPasswordRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.reset("bat-ky", "ngan"));
    }
}
