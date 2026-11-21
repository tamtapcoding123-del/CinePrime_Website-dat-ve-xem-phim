package vn.cineprime.asm.junit;

import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Y2/Y3 - JUnit 5 + MockMvc: kiểm thử tầng controller (Pages, AccountController, SecurityConfig) qua bộ lọc bảo mật thật,
 * chạy trên MySQL schema cineprime_test (không dùng H2). Chỉ chạy khi có biến môi trường TEST_DB_URL ... /cineprime_test.
 */
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = "jdbc:mysql:.*[/]cineprime_test(?:\\?.*)?")
@SpringBootTest(properties = {"cineprime.seed=false", "cineprime.jobs-enabled=false", "cineprime.mail-enabled=false"})
@DisplayName("Controller + Security qua MockMvc (MySQL cineprime_test)")
class ControllerMvcTest {
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> System.getenv("TEST_DB_URL"));
        r.add("spring.datasource.username", () -> System.getenv().getOrDefault("TEST_DB_USER", "cineprime"));
        r.add("spring.datasource.password", () -> System.getenv().getOrDefault("TEST_DB_PASSWORD", ""));
    }

    @Autowired WebApplicationContext context;
    MockMvc mvc;

    /** MockMvc không tự đặt servletPath; ứng dụng (PageAdvice, SecurityConfig, AccountController) đọc servletPath nên phải gán cùng đường dẫn. */
    static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder G(String path) { return get(path).servletPath(path.equals("/") ? "" : path); }
    static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder P(String path) { return post(path).servletPath(path.equals("/") ? "" : path); }
    /** So khớp nội dung trả về đã giải mã UTF-8 và bỏ mã hoá thực thể HTML như &aacute; (MockMvc mặc định ISO-8859-1 nên chữ có dấu sẽ sai nếu không chỉ rõ). */
    static org.springframework.test.web.servlet.ResultMatcher utf8(org.hamcrest.Matcher<String> matcher) {
        return result -> org.hamcrest.MatcherAssert.assertThat(org.springframework.web.util.HtmlUtils.htmlUnescape(result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)), matcher);
    }

    @BeforeEach void setUp() { mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }

    @Test @DisplayName("ID059 GET / trang chủ công khai trả 200 HTML UTF-8")
    void homeIsPublic() throws Exception {
        mvc.perform(G("/")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/html")).andExpect(utf8(containsString("CinePrime")));
    }

    @Test @DisplayName("ID060 Các trang công khai /movies /showtimes /cinemas /offers trả 200")
    void publicCatalogPages() throws Exception {
        for (String path : new String[]{"/movies", "/showtimes", "/cinemas", "/offers", "/articles"})
            mvc.perform(G(path)).andExpect(status().isOk());
    }

    @Test @DisplayName("ID061 Trang đăng nhập có form email + mật khẩu + CSRF")
    void loginPageHasForm() throws Exception {
        mvc.perform(G("/login")).andExpect(status().isOk())
            .andExpect(utf8(allOf(containsString("name=\"email\""), containsString("name=\"password\""), containsString("_csrf"), containsString("Đăng nhập"))));
    }

    @Test @DisplayName("ID062 Trang đăng ký có đủ họ tên, email, mật khẩu (minlength 10)")
    void registerPageHasForm() throws Exception {
        mvc.perform(G("/register")).andExpect(status().isOk())
            .andExpect(utf8(allOf(containsString("name=\"name\""), containsString("name=\"email\""), containsString("minlength=\"10\""))));
    }

    @Test @DisplayName("ID063 Phim không tồn tại không gây lỗi 5xx")
    void unknownMovieIsNotServerError() throws Exception {
        int code = mvc.perform(G("/movies/" + UUID.randomUUID())).andReturn().getResponse().getStatus();
        assertTrue(code >= 400 && code < 500, "mong 4xx, nhận " + code);
    }

    @Test @DisplayName("ID064 Chưa đăng nhập vào /admin /staff /account /booking/1 bị chuyển tới /login")
    void protectedPagesRedirectToLogin() throws Exception {
        for (String path : new String[]{"/admin", "/staff", "/account", "/booking/1", "/orders/1"})
            mvc.perform(G(path)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test @DisplayName("ID065 API admin khi chưa đăng nhập trả 401 JSON SESSION_EXPIRED (không chuyển hướng HTML)")
    void adminApiReturnsJson401() throws Exception {
        mvc.perform(G("/api/admin/prices")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("SESSION_EXPIRED"));
    }

    @Test @DisplayName("ID066 Khách hàng (CUSTOMER) vào /admin bị 403; vào /staff cũng 403")
    void customerCannotOpenOperations() throws Exception {
        mvc.perform(G("/admin").with(user("khach@cineprime.vn").roles("CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(G("/staff").with(user("khach@cineprime.vn").roles("CUSTOMER"))).andExpect(status().isForbidden());
    }

    @Test @DisplayName("ID067 STAFF không vào được /admin")
    void staffCannotOpenAdmin() throws Exception {
        mvc.perform(G("/admin/manage/users").with(user("nv@cineprime.vn").roles("STAFF"))).andExpect(status().isForbidden());
    }

    @Test @DisplayName("ID068 POST /register thiếu CSRF bị 403 (chặn giả mạo yêu cầu)")
    void registerWithoutCsrfForbidden() throws Exception {
        mvc.perform(P("/register").param("name", "An").param("email", "an@cineprime.vn").param("password", "matkhau12345")).andExpect(status().isForbidden());
    }

    @Test @DisplayName("ID069 POST /register mật khẩu 9 ký tự bị từ chối 400 với thông báo tiếng Việt")
    void registerShortPassword() throws Exception {
        mvc.perform(P("/register").with(csrf()).param("name", "An").param("email", "ngan-" + UUID.randomUUID() + "@cineprime.vn").param("password", "123456789"))
            .andExpect(status().isBadRequest()).andExpect(utf8(containsString("ít nhất 10 ký tự")));
    }

    @Test @DisplayName("ID070 POST /register email sai định dạng bị từ chối 400")
    void registerBadEmail() throws Exception {
        mvc.perform(P("/register").with(csrf()).param("name", "An").param("email", "khong-phai-email").param("password", "matkhau12345"))
            .andExpect(status().isBadRequest()).andExpect(utf8(containsString("Kiểm tra email và họ tên")));
    }

    @Test @DisplayName("ID071 Đăng ký hợp lệ -> chuyển /login?registered; đăng nhập đúng -> /account; sai mật khẩu -> /login?error")
    void registerThenLogin() throws Exception {
        String email = "mvc-" + UUID.randomUUID().toString().substring(0, 8) + "@cineprime.vn";
        mvc.perform(P("/register").with(csrf()).param("name", "Người Dùng MVC").param("email", email).param("password", "matkhau12345"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?registered"));
        mvc.perform(P("/login").with(csrf()).param("email", email).param("password", "matkhau12345"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/account"));
        mvc.perform(P("/login").with(csrf()).param("email", email).param("password", "sai-mat-khau"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?error"));
    }

    @Test @DisplayName("ID072 Đăng ký trùng email lần 2 bị từ chối bằng thông báo chung (không lộ email tồn tại)")
    void duplicateRegistration() throws Exception {
        String email = "dup-" + UUID.randomUUID().toString().substring(0, 8) + "@cineprime.vn";
        mvc.perform(P("/register").with(csrf()).param("name", "A").param("email", email).param("password", "matkhau12345")).andExpect(status().is3xxRedirection());
        mvc.perform(P("/register").with(csrf()).param("name", "B").param("email", email.toUpperCase()).param("password", "matkhau12345"))
            .andExpect(status().isBadRequest()).andExpect(utf8(containsString("Không thể đăng ký email này")));
    }

    @Test @DisplayName("ID073 IPN MoMo giả (sai chữ ký) không bao giờ trả 2xx")
    void forgedMomoIpnRejected() throws Exception {
        MvcResult r = mvc.perform(P("/payments/momo/ipn").contentType("application/json")
            .content("{\"partnerCode\":\"X\",\"orderId\":\"FAKE\",\"amount\":1,\"resultCode\":0,\"signature\":\"deadbeef\"}")).andReturn();
        int code = r.getResponse().getStatus();
        assertTrue(code >= 400 && code < 500, "IPN giả phải bị 4xx, nhận " + code);
    }

    @Test @DisplayName("ID074 Tài nguyên tĩnh /css/site.css công khai và trả CSS")
    void staticCssIsPublic() throws Exception {
        mvc.perform(G("/css/site.css")).andExpect(status().isOk());
    }

    @Test @DisplayName("ID075 Header CSP chặn script ngoài (default-src 'self')")
    void cspHeaderPresent() throws Exception {
        mvc.perform(G("/")).andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
    }
}
