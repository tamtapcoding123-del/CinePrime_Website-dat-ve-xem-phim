package vn.cineprime.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/** Y3 - JUnit 5 (bảo mật): AccountController.localPath chặn open-redirect và header injection ở tham số next. Cùng package vì hàm là package-private. */
class OpenRedirectGuardTest {
    @ParameterizedTest(name = "ID052 \"{0}\" là đường dẫn nội bộ hợp lệ")
    @ValueSource(strings = {"/", "/movies/phim-a", "/orders/12?x=1"})
    void localPathsAccepted(String path) { assertTrue(AccountController.localPath(path)); }

    @ParameterizedTest(name = "ID053 đường dẫn nguy hiểm bị chặn #{index}")
    @ValueSource(strings = {"//evil.com", "https://evil.com", "evil.com/x", "/a\\b", "/a\r\nSet-Cookie: x=1", "/a\u0000b"})
    void unsafePathsRejected(String path) { assertFalse(AccountController.localPath(path)); }

    @Test @DisplayName("ID054 null và đường dẫn dài hơn 512 ký tự bị chặn")
    void nullAndTooLongRejected() {
        assertFalse(AccountController.localPath(null));
        assertFalse(AccountController.localPath("/" + "a".repeat(512)));
        assertTrue(AccountController.localPath("/" + "a".repeat(511)));
    }
}
