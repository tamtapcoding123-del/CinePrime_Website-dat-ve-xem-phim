package vn.cineprime.asm.junit;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import vn.cineprime.media.MediaService;
import static org.junit.jupiter.api.Assertions.*;

/** Y3 - JUnit 5: kiểm tra upload poster/banner/trailer (định dạng, kích thước, nội dung thật). Biểu mẫu Admin gọi MediaService.upload. */
@DisplayName("MediaService.upload - kiểm tra file tải lên")
class MediaUploadValidationTest {
    @TempDir Path dir;
    MediaService media;
    @BeforeEach void setUp() { media = new MediaService(dir.toString()); }

    static byte[] png(int w, int h) throws Exception {
        var out = new ByteArrayOutputStream(); ImageIO.write(new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB), "png", out); return out.toByteArray();
    }
    String message(MockMultipartFile f, String kind) { return assertThrows(IllegalArgumentException.class, () -> media.upload(f, kind)).getMessage(); }

    @Test @DisplayName("ID041 Không chọn file thì báo lỗi")
    void emptyFileRejected() { assertEquals("Chọn file để tải lên.", message(new MockMultipartFile("f", new byte[0]), "poster")); }
    @Test @DisplayName("ID042 Loại trường upload lạ bị từ chối")
    void unknownKindRejected() throws Exception { assertEquals("Loại trường upload không hợp lệ.", message(new MockMultipartFile("f", "a.png", "image/png", png(10, 10)), "script")); }
    @Test @DisplayName("ID043 File văn bản đội lốt content-type image/png bị từ chối (đọc nội dung, không tin tên file)")
    void fakeImageRejected() { assertTrue(message(new MockMultipartFile("f", "a.png", "image/png", "khong phai anh".getBytes()), "poster").startsWith("Không đọc được ảnh")); }
    @Test @DisplayName("ID044 Định dạng không được phép (GIF/PDF/SVG/HTML) bị từ chối")
    void disallowedTypesRejected() {
        for (String type : new String[]{"image/gif", "application/pdf", "image/svg+xml", "text/html"})
            assertEquals("Chỉ nhận ảnh JPG, PNG hoặc WEBP, tối đa 5 MB.", message(new MockMultipartFile("f", "x", type, new byte[]{1, 2, 3}), "poster"));
    }
    @Test @DisplayName("ID045 Ảnh PNG hợp lệ được lưu với tên UUID .png, trả đường dẫn /media/...")
    void validPngSaved() throws Exception {
        String url = media.upload(new MockMultipartFile("f", "poster.png", "image/png", png(300, 450)), "poster");
        assertTrue(url.matches("/media/[a-f0-9-]{36}\\.png"));
        assertTrue(dir.resolve(url.substring(7)).toFile().isFile());
    }
    @Test @DisplayName("ID046 Ảnh lớn hơn 8000px mỗi chiều bị từ chối")
    void oversizeDimensionRejected() throws Exception { assertEquals("Ảnh tối đa 8000px mỗi chiều.", message(new MockMultipartFile("f", "a.png", "image/png", png(8001, 10)), "poster")); }
    @Test @DisplayName("ID047 Banner phải là ảnh ngang tỉ lệ từ 4:3 và rộng tối thiểu 560px")
    void bannerMustBeLandscape() throws Exception {
        assertTrue(message(new MockMultipartFile("f", "b.png", "image/png", png(300, 450)), "banner").startsWith("Ảnh banner cần là ảnh ngang"));
        assertTrue(media.upload(new MockMultipartFile("f", "b.png", "image/png", png(1920, 640)), "banner").startsWith("/media/"));
    }
    @Test @DisplayName("ID048 Trailer chỉ nhận MP4 có chữ ftyp; MP4 giả bị từ chối")
    void trailerMustBeRealMp4() {
        assertEquals("Trailer MP4 không hợp lệ.", message(new MockMultipartFile("f", "t.mp4", "video/mp4", "0123456789AB".getBytes()), "trailer"));
        byte[] ok = {0, 0, 0, 24, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm'};
        assertDoesNotThrow(() -> media.upload(new MockMultipartFile("f", "t.mp4", "video/mp4", ok), "trailer"));
    }
    @Test @DisplayName("ID049 read() từ chối tên file lạ (chống path traversal)")
    void readRejectsTraversal() {
        assertThrows(IllegalArgumentException.class, () -> media.read("../../etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> media.read("abc.jpg"));
    }
    @Test @DisplayName("ID050 image(): chỉ nhận /media/<uuid> đã tồn tại hoặc ảnh tĩnh của ứng dụng; link ngoài bị từ chối")
    void imageReferencePolicy() {
        assertEquals("", media.image("  "));
        assertThrows(IllegalArgumentException.class, () -> media.image("https://evil.example/x.png"));
        assertThrows(IllegalArgumentException.class, () -> media.image("/media/00000000-0000-0000-0000-000000000000.png"));
    }
    @Test @DisplayName("ID051 Quy tắc tỉ lệ ảnh: wide (>=1600 và >=16:9), framed (>=560 và >=4:3), portrait")
    void aspectRules() {
        assertTrue(MediaService.wide(1920, 640)); assertFalse(MediaService.wide(1599, 400)); assertFalse(MediaService.wide(1600, 1000));
        assertTrue(MediaService.framed(560, 420)); assertFalse(MediaService.framed(559, 300)); assertFalse(MediaService.framed(1000, 900));
        assertTrue(MediaService.portrait(400, 500)); assertFalse(MediaService.portrait(500, 500));
    }
}
