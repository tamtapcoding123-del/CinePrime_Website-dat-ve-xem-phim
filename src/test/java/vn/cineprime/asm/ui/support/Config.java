package vn.cineprime.asm.ui.support;

/** Cấu hình chạy Selenium, đọc từ -D (system property) hoặc biến môi trường; không ghi mật khẩu vào mã nguồn. */
public final class Config {
    private Config() {}

    /** Địa chỉ ứng dụng đang chạy (mặc định cổng 8081 chạy trên schema cineprime_test). */
    public static String baseUrl() { return value("base.url", "BASE_URL", "http://localhost:8081").replaceAll("/+$", ""); }
    /** chrome (mặc định) hoặc edge. */
    public static String browser() { return value("browser", "BROWSER", "chrome").toLowerCase(); }
    /** false khi muốn nhìn trình duyệt chạy: -Dheadless=false. */
    public static boolean headless() { return !"false".equalsIgnoreCase(value("headless", "HEADLESS", "true")); }
    /** Mật khẩu dữ liệu demo (DEMO_PASSWORD khi khởi động ứng dụng với DEMO_SEED=true). */
    public static String demoPassword() { return value("demo.password", "DEMO_PASSWORD", ""); }
    public static String adminEmail() { return value("admin.email", "ADMIN_EMAIL", "admin@cineprime.local"); }
    public static String customerEmail() { return value("customer.email", "CUSTOMER_EMAIL", "customer@cineprime.local"); }
    public static int waitSeconds() { return Integer.parseInt(value("wait.seconds", "WAIT_SECONDS", "10")); }

    private static String value(String property, String env, String fallback) {
        String p = System.getProperty(property);
        if (p != null && !p.isBlank()) return p;
        String e = System.getenv(env);
        return e != null && !e.isBlank() ? e : fallback;
    }
}
