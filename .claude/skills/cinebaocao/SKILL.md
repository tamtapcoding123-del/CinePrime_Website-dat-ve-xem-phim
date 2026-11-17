---
name: cinebaocao
description: Skill làm file Word báo cáo đồ án/dự án CinePrime đúng trình bày và bộ khung của bản mẫu Mau-Bao-Cao-JAV102.pdf (bìa FPT, lời cảm ơn, cam kết, mục lục tự động, thuật ngữ, mở đầu, 6 chương, tài liệu tham khảo). Tự lấy ảnh Use case/Sitemap/Activity/ERD của dự án điền vào. Dùng khi người dùng nói "làm báo cáo", "file báo cáo", "báo cáo word", "viết chương …", "điền báo cáo", hoặc gọi /cinebaocao [tạo | chương <số> | ảnh giao diện | soát].
argument-hint: "[tạo | chương <I..VI> | ảnh giao diện | soát] [yêu cầu]"
---

# /cinebaocao — Báo cáo Word theo bản mẫu JAV102

Đối số: `$ARGUMENTS`. Trống = hỏi người dùng đang ở bước nào, rồi theo §2.

Mẫu: `D:\0_JAV102\Mau-Bao-Cao-JAV102.pdf` (đồ án WellFitness, 107 trang, FPT Polytechnic). Skill này **không** viết lại nội dung mẫu; nó sao đúng **cách trình bày, thứ tự mục, cách đưa nội dung** của mẫu rồi điền nội dung thật của CinePrime.

## ĐỌC TRƯỚC — trạng thái báo cáo và cách tiếp tục (cập nhật 17/11/2026)

Khi người dùng gọi skill ở phiên mới, **đừng hỏi lại những gì dưới đây**. Làm theo đúng yêu cầu họ mô tả trong phiên đó, dựa trên hiện trạng này.

**Tệp làm việc (nguồn sự thật = `bao-cao.txt`, không phải khung):**
- `D:\0_JAV102\CinePrime_Project\Báo cáo\bao-cao.txt` — nội dung báo cáo đầy đủ hiện tại (đã viết hết bìa → Chương VI + Phụ lục). Mọi chỉnh sửa/bổ sung sửa **file này** rồi sinh lại Word. `khung-baocao.txt` chỉ để dựng báo cáo mới từ đầu.
- Word đã giao: `BaoCao-CinePrime.docx` (cũ), `-v2.docx`, `-v3.docx` (bản đầy đủ mới nhất, 76 trang). Người dùng duyệt/sửa tay trong Word, nên **luôn sinh ra tên mới** (`-v4`, `-v5`…), không ghi đè bản họ có thể đang mở (Word báo "already open elsewhere"). Chỉ `bao-cao.txt`, `Ảnh giao diện\` và các `.docx` nằm trong `Báo cáo\`; PDF soát để ở thư mục tạm của session.
- Ảnh: `Sơ đồ\Usecase` (UC01–08, SM01), `Sơ đồ\Activity-Diagrams` (AD01–06), `Sơ đồ\ERD` (ERD01-toan-bo.png + .docx), **`Sơ đồ\Wireframe` (WF01–WF18)**, `Báo cáo\Ảnh giao diện\` (KQ01–KQ03 ảnh app chạy thật cho Chương V), `.claude\skills\cinebaocao\hinh-ly-thuyet\` (client-server, mvc).
- Sinh Word: lệnh ở §2 bước 5 (`tao-baocao.ps1 -NoiDung "Báo cáo\bao-cao.txt" -Ra "Báo cáo\BaoCao-CinePrime-vN.docx" -Pdf`, mất ~1–2 phút), rồi chuyển `.pdf` ra thư mục tạm để soát bằng PyMuPDF (đọc/ghép trang thành ảnh; máy không có `pdftoppm` nên tool Read không đọc trực tiếp PDF được). Giao bằng SendUserFile.

**Đã hoàn thành trong `bao-cao.txt`:** Bìa (Đồ án tốt nghiệp, CNTT, Lập trình web, Đà Nẵng 11/2026) · Lời cảm ơn · Cam kết · 3 mục lục · Phần mở đầu (1–3, Bảng 1 kế hoạch chia đều 3 người) · Chương I (7 mục, có hình Client-Server và MVC) · Chương II (1 nghiệp vụ; 2.1 use case ×8; 2.2 sitemap; 2.3 ERD chèn từ Word cũ 27 bảng; 2.4 activity ×6; 3 giao diện = 18 wireframe + bảng đặc tả) · Chương III (cấu trúc, CSDL, 5 chức năng có mã nguồn) · Chương IV (6 nhóm kiểm thử + bảng tổng 159 ca) · Chương V (chạy local có ảnh thật; Vercel ghi "dự kiến, chưa thực hiện") · Chương VI · Phụ lục.

**Quyết định/thông tin người dùng đã đưa (không hỏi lại):**
- Nhóm 3 người, giảng viên Thầy Hoàng Hà (bảng ở §3). Không có bảng thuật ngữ. Đúng phong cách mẫu JAV102: ngắn gọn, đi thẳng trọng tâm, gạch đầu dòng.
- Wireframe thay ảnh chụp ở Chương II §3, lưu `Sơ đồ\Wireframe`, theo phong cách website (§3).
- ERD: **giữ file cũ 27 bảng** dù CSDL đã 37 bảng — chỉ vẽ lại (`/cineerd` rồi dựng lại báo cáo) khi người dùng yêu cầu.
- Chương V: người dùng muốn "Deploy trên Vercel" nhưng chưa có hosting/tài khoản → giữ "dự kiến"; khi họ cung cấp và deploy xong mới viết bước thật + ảnh.
- Ngày/phân công trong Bảng 1 do tôi gán từ lịch sử git (06–14/11/2026) và chia đều; người dùng có thể sửa.

**Việc còn mở (làm khi người dùng yêu cầu):** cập nhật ERD đủ bảng · deploy Vercel thật · thêm wireframe/đặc tả cho tính năng mới (hạng thành viên khách, ví voucher, quà…) · bổ sung mục lục/số liệu khi dự án đổi (chạy lại `mvnw test` lấy số ca mới) · sửa theo góp ý duyệt tay của người dùng (họ đọc bản v3 và sẽ gửi góp ý).

**Cách làm việc người dùng muốn:** trả lời tiếng Việt, **làm nhanh và gọn**, không chạy việc nền dài không cần thiết (từng bị phàn nàn một task chạy hơn 1 giờ). Chỉ chạy lại test/dựng JAR khi cần số liệu mới; nếu đổi chỉ chữ/ảnh thì sinh Word thẳng. Hỏi người dùng khi thiếu thông tin thật sự (họ cho phép), kèm đọc hiểu dự án trước. Trước khi làm: đọc lại mẫu PDF `D:\0_JAV102\Mau-Bao-Cao-JAV102.pdf` (phần liên quan) và bản báo cáo gần nhất để giữ đúng giọng và bố cục.

**Cạm bẫy đã gặp:**
- Edge headless với cửa sổ rộng 1280 mà cao > ~1400 sẽ tự co chiều rộng → mất phần bên phải ảnh (xem quy tắc wireframe ở §3).
- Không `taskkill /IM java.exe` (tắt luôn app 8080 của người dùng); chỉ dừng tiến trình mình chạy.
- `sed` không khớp được chuỗi tiếng Việt có dấu trên máy này — dùng Python (`python -I`, `sys.stdout.reconfigure(encoding='utf-8')`) để thay thế chuỗi.
- Test kiểm thử cần biến môi trường `TEST_DB_*` trỏ `cineprime_test` (xem memory `local-mysql-access`); app 8081 chạy từ JAR với `DEMO_SEED`; không ghi mật khẩu DB vào báo cáo hay SKILL.
- Hình rất cao (WF01) tự co theo chiều cao ≤ 21 cm; nếu chữ quá nhỏ thì chia ảnh thành hai hoặc cắt bớt khối.

## 0. Tệp trong skill

| Tệp | Việc |
|---|---|
| [tao-baocao.ps1](tao-baocao.ps1) | Sinh `.docx` (Word COM, như `/cineerd`) từ một file `.txt` nội dung. **Không viết Word/XML bằng tay.** |
| [khung-baocao.txt](khung-baocao.txt) | Bộ khung toàn báo cáo: đủ mục theo mẫu, ghi chú nguồn dữ liệu, ảnh đã trỏ sẵn tới `Sơ đồ\…`, chỗ cần điền là `<<…>>`. |
| [cu-phap.md](cu-phap.md) | Cú pháp file nội dung (tiêu đề, gạch đầu dòng, bảng, hình, mã, chèn file). |
| `logo-fpt-education.png` | Logo bìa lấy từ mẫu (nền trong suốt). |
| `hinh-ly-thuyet\` | `client-server.png`, `mvc.png` — hình minh họa chương I lấy từ mẫu. |

Sau khi sửa `tao-baocao.ps1` phải giữ **UTF-8 có BOM**: `$f='…\tao-baocao.ps1'; [IO.File]::WriteAllText($f,[IO.File]::ReadAllText($f,[Text.Encoding]::UTF8),(New-Object Text.UTF8Encoding $true))`. Script dùng Word đã cài; nếu Word của người dùng đang mở, script chỉ đóng file của nó, không `Quit`.

## 1. Bản mẫu được phân tích — quy tắc trình bày (đã đo từ PDF)

**Trang & chữ.** A4; lề 2,54 cm bốn phía; Times New Roman đen toàn bài; thân bài 12 pt, giãn dòng 1,15, căn đều hai bên; trang Lời cảm ơn/Cam kết 14 pt. Tiêu đề chương 14 pt đậm; mọi tiêu đề mục 12 pt đậm; chữ HOA cho mục cấp 1 (`1. MÔ HÌNH CÔNG NGHỆ`), thường cho mục con. Không màu, không tô nền (trừ hàng tiêu đề bảng xám #D9D9D9 và khối mã tối).

**Chân trang** (mọi trang trừ bìa): đường kẻ ngang trên; trái `07.01.16- BM/FPL/HDCV/FE` 11 pt; phải số trang (Arial 10,5); số bắt đầu từ 0 ở bìa nên Lời cảm ơn là trang 1. Không có đầu trang.

**Bìa** (khung viền đôi đen quanh trang): 3 dòng đơn vị (FPT / Trường / Bộ môn) 14 đậm → dòng gạch `-----` → logo FPT Education → **ĐỒ ÁN TỐT NGHIỆP** 24 đậm → Ngành/Chuyên ngành 16 (giá trị đậm) → `ĐỀ TÀI:` 14 đậm → tên đề tài 24 đậm → khối Giảng viên hướng dẫn / Sinh viên thực hiện (nhãn nghiêng căn phải, tên đậm) → `Địa điểm, MM/YYYY` 14 đậm dưới đáy.

**Thứ tự báo cáo** (mỗi mục ở trang mới):

1. Bìa → LỜI CẢM ƠN (giữa, 2 đoạn + câu đậm nghiêng) → CAM KẾT (đánh số 1–3, "Đại diện nhóm sinh viên thực hiện" + họ tên)
2. MỤC LỤC (chương đậm, mục con thụt dần, chấm dẫn + số trang) → MỤC LỤC HÌNH ẢNH → MỤC LỤC BẢNG BIỂU (mẫu còn bảng Danh sách thuật ngữ & từ viết tắt — **CinePrime bỏ bảng này**, xem §3)
3. **PHẦN MỞ ĐẦU**: 1 Phân tích hiện trạng · 2 Mục đích (2.1 sản phẩm, 2.2 mục tiêu hệ thống, 2.3 tính năng cơ bản, 2.4 đối tượng sử dụng, 2.5 công nghệ) · 3 Lập kế hoạch dự án (bảng 6 cột, hàng nhóm in đậm trải ngang)
4. **CHƯƠNG I: CƠ SỞ LÝ THUYẾT** — mỗi công nghệ một mục: định nghĩa → `- Ưu điểm:` → `- Nhược điểm:` (ý con bằng `+`)
5. **CHƯƠNG II: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG** — 1 Phân tích nghiệp vụ theo vai trò · 2 Thiết kế hệ thống (2.1 Use case, 2.2 Sitemap, 2.3 ERD + bảng "Thực thể …", 2.4 Activity) · 3 Thiết kế giao diện và đặc tả (mỗi màn: ảnh → `Đặc tả:` → bảng)
6. **CHƯƠNG III: TRIỂN KHAI MÃ NGUỒN** — 1 Cấu trúc mã nguồn · 2 Kết nối CSDL · 3 Các chức năng chính (mỗi chức năng: `- Mô tả:` với các bước `+` → `- Mã nguồn:` khối mã tối)
7. **CHƯƠNG IV: KIỂM THỬ** — mỗi chức năng: `- Mục tiêu:` → bảng STT | Tình huống kiểm thử | Dữ liệu nhập | Kết quả mong đợi | Pass/Fail
8. **CHƯƠNG V: TRIỂN KHAI** — từng bước `B1:, B2:…` kèm ảnh chụp từng bước và ảnh "Kết quả"
9. **CHƯƠNG VI: HẠN CHẾ VÀ HƯỚNG PHÁT TRIỂN** — 1 Hạn chế · 2 Hướng phát triển · 3 Kết luận → PHỤ LỤC: TÀI LIỆU THAM KHẢO (gạch đầu dòng "Tên: URL")

**Hình/bảng.** Hình căn giữa, chú thích dưới `Hình N: …`; bảng chú thích dưới `Bảng N: …` (trường SEQ, tự vào Mục lục hình/bảng). Bảng thực thể 5 cột (Thuộc tính | Kiểu dữ liệu | Bắt buộc | Ràng buộc | Mô tả), hàng tiêu đề xám, hàng khóa chính đậm. Bảng đặc tả giao diện: 3 dòng nhãn xám (Giao diện/Mô tả/Truy cập) → dải `Đặc tả giao diện` → bảng Mục | Loại component | Mô tả.

**Giọng văn.** Gạch đầu dòng ngắn, mỗi dòng một ý, thường dạng "Tên ý: giải thích"; không văn xuôi dài (trừ Lời cảm ơn).

## 2. Quy trình

1. **Chuẩn bị.** Thư mục làm việc `D:\0_JAV102\CinePrime_Project\Báo cáo\` (tạo nếu chưa có): `bao-cao.txt` (nội dung), `Ảnh giao diện\`, và kết quả `BaoCao-CinePrime.docx`. Lần đầu: chép [khung-baocao.txt](khung-baocao.txt) thành `bao-cao.txt`. Thư mục này là nơi người dùng mở Word; **không** để JSON/PDF tạm lẫn vào (PDF soát đặt thư mục tạm của session).
2. **Hỏi người dùng đúng những thứ không thể tự biết**, một lần (nhóm, giảng viên, loại báo cáo, chuyên ngành đã có — chỉ hỏi cái còn thiếu): phân công và ngày từng hạng mục (Bảng 1), nơi/tháng ghi bìa. Còn lại **tự điền từ dự án**, không hỏi:
   | Phần | Nguồn |
   |---|---|
   | Hiện trạng, mục đích, tính năng, nghiệp vụ | Plan.md §1–§2, README "Tổng quan công nghệ và nghiệp vụ" |
   | Công nghệ, thư viện | README + `pom.xml` |
   | Use case, Sitemap, Activity, ERD | ảnh sẵn trong `Sơ đồ\Usecase`, `Activity-Diagrams`, `ERD` (khung đã trỏ tên file); thiếu/cũ → `/cinediagram`, `/cineactivity`, `/cineerd` |
   | Bảng thực thể 2.3.x | file Word `ERD01-toan-bo.docx` (`[chen]`) |
   | Mã nguồn chương III | trích đoạn thật từ `src/main/java/vn/cineprime/…` |
   | Kiểm thử | `src/test/java`, `target/surefire-reports`, README "Kiểm thử" |
   | Hạn chế/hướng phát triển | README/Plan: phần **chưa** kiểm chứng + lộ trình sau |
3. **Điền theo chương, từng lượt** (người dùng nói "viết chương III" thì chỉ làm chương đó): thay `<<…>>` trong `bao-cao.txt`; cắt mục không có trong dự án (không bịa), giữ đánh số liên tục. Với mã nguồn: đọc file thật, trích 15–40 dòng đúng chức năng, tuyệt đối không có bí mật (mật khẩu DB, khóa MoMo, token) — giữ `${ENV}`.
4. **Ảnh giao diện** (chương II §3 và chương V): chụp bằng trình duyệt tích hợp trên app **cổng 8081/schema `cineprime_test`** với `DEMO_SEED=true` (không đụng 8080 của IntelliJ, không đụng `cineprime`). Khách chụp 1366×768; lưu `Báo cáo\Ảnh giao diện\GDnn-<tên>.png`. Không chụp màn có dữ liệu/ khóa thật. Nếu chưa chụp được, để dòng `[hinh]` đã trỏ sẵn — script sẽ ghi chữ đỏ `[THIẾU ẢNH]` thay vì lỗi.
5. **Sinh Word:**
   `powershell -NoProfile -ExecutionPolicy Bypass -File D:\0_JAV102\CinePrime_Project\.claude\skills\cinebaocao\tao-baocao.ps1 -NoiDung "Báo cáo\bao-cao.txt" -Ra "Báo cáo\BaoCao-CinePrime.docx" -Pdf`
   `-Pdf` ghi thêm `.pdf` cạnh `.docx` — **chỉ để soát**; soát xong xóa nó. Mục lục/số trang cập nhật tự động trong script; người dùng mở Word chỉ cần bấm F9 nếu sửa tiếp.
6. **Soát** (§5) rồi giao đường dẫn `.docx`, số trang, danh sách mục còn `<<…>>` hoặc thiếu ảnh, và những điều chưa kiểm chứng (ghi rõ trong chương IV/VI).

## 3. Quy tắc nội dung (không thương lượng)

- **Nhóm thực hiện (người dùng cung cấp 14/11/2026, dùng cho bìa, Cam kết, Bảng 1):**
  | Họ tên | Vai trò | MSSV | Viết tắt trong Bảng 1 |
  |---|---|---|---|
  | Nguyễn Minh Tâm | Nhóm trưởng | TD01729 | TamNM |
  | Lê Văn Gia Bảo | Thành viên | TD01611 | BaoLVG |
  | Trần Gia Huy | Thành viên | TD01616 | HuyTG |
  Giảng viên hướng dẫn: **Thầy Hoàng Hà** (người dùng cho 17/11/2026; bìa ghi "Thầy Hoàng Hà", trong câu ghi "thầy Hoàng Hà"). Cam kết do Nguyễn Minh Tâm ký (nhóm trưởng).
- **Không có bảng Danh sách thuật ngữ & từ viết tắt** (người dùng bỏ). Không thêm lại; thuật ngữ cần giải thích thì viết ngay trong câu.
- **Bảng 1 Lập kế hoạch: chia đều việc cho 3 thành viên** (mỗi người ~1/3 số hạng mục lá; hạng mục chung như Kiểm thử ghi cả ba, như mẫu ghi nhiều tên). Cột Người làm dùng tên viết tắt ở bảng trên, không để `[SV]`. Khi thêm/bớt hạng mục phải cân lại cho đều.
- **Chương II §3 dùng Wireframe, không dùng ảnh chụp.** Mỗi màn: tiêu đề mục → ảnh wireframe → `Đặc tả:` → bảng đặc tả (Giao diện/Mô tả/Truy cập + bảng Mục | Loại component | Mô tả). Ảnh wireframe lưu ở **`D: _JAV102\CinePrime_Project\Sơ đồ\Wireframe\WFnn-<tên>.png`** (người dùng chỉ định 17/11/2026), đúng phong cách mẫu: khung xám, ô ảnh có dấu X, thanh xám thay chữ dài, đủ đầu trang/chân trang; dùng phong cách website (Studio Cinema Pearl: nền xám ngọc trai #DFE4EC/#F4F6F9, góc vát chéo trên-trái và dưới-phải, nút chính đỏ #C83728, nút phụ slate #1E2532, phông Be Vietnam Pro + Plus Jakarta Sans). Bố cục lấy từ template `.pug` thật; thành phần trong bảng đặc tả phải có thật trên màn. Hiện có 18 màn: 3.1 Trang chủ · 3.2 Danh sách phim · 3.3 Chi tiết phim · 3.4 Lịch chiếu · 3.5 Chọn ghế · 3.6 Đơn và thanh toán · 3.7 Đăng nhập · 3.8 Đăng ký · 3.9 Tài khoản · 3.10 Admin Tổng quan · 3.11 Admin Danh sách phim · 3.12 Admin Form phim · 3.13 Admin Lịch chiếu tuần · 3.14 Admin Phòng và sơ đồ ghế · 3.15 Admin Đơn hàng · 3.16 Admin Hạng thành viên · 3.17 Staff Check-in · 3.18 Staff Bán vé tại quầy. Thêm tính năng mới thì vẽ thêm màn tương ứng.
  Cách dựng wireframe (không đưa vào repo): viết HTML + CSS trong thư mục tạm của session (khối đặt ảnh `.ph` có chéo X, chamfer bằng `clip-path`, font woff2 sao từ `src/main/resources/static/fonts`), chụp bằng `msedge --headless=new --force-device-scale-factor=2 --window-size=<rộng>,<cao>`; **trang rộng 1280 (Admin/Staff) phải đặt chiều cao ≤ 1400**, cao hơn Edge tự thu nhỏ chiều rộng và cắt mất nội dung bên phải; sau đó cắt phần trắng cuối ảnh. Chỉ giao PNG vào `Sơ đồ\Wireframe\`.
- **Chương IV: ca "thủ công" phải thật.** Đăng ký/Đăng nhập: chạy app JAR trên cổng 8081 (schema `cineprime_test`, `DEMO_SEED=true`) rồi gửi HTTP thật (curl, kèm mã CSRF), ghi đúng mã trả về. Các nhóm còn lại lấy từ test JUnit có sẵn (`BookingIntegrationTest` …), tên ca bám theo tên test; số liệu tổng lấy từ `mvnw test` chạy lại cùng ngày (17/11/2026: 159 ca, 0 lỗi, 0 bỏ qua; lớp BookingIntegrationTest 129, ShowtimeExcelTest 15, AdminFormsTest 4, MediaServiceTest 4, MovieLifecycleTest 3, ReportServiceTest 3, MomoSignatureTest 1). Cột Pass/Fail rộng ≥ 2,3 để không bị xuống dòng.
- **Chương V: người dùng chọn "Deploy trên Vercel" (17/11/2026), nhưng dự án chưa deploy.** Viết: 1 Chuẩn bị môi trường, 2 Cấu hình và chạy ứng dụng (lệnh đúng + ảnh chụp thật `KQ01…KQ03` lưu ở `Báo cáo\Ảnh giao diện\`), 3 Triển khai lên Vercel **ghi rõ "dự kiến, chưa thực hiện"** (Vercel chạy Spring Boot qua Docker/Container runtime, cổng `$PORT`, MySQL và nơi lưu ảnh phải ở ngoài). Chỉ chuyển thành các bước B1, B2… kèm ảnh khi người dùng cung cấp tài khoản/hosting và việc deploy thực sự xong.
- **Mục 2.3 ERD hiện dùng file Word ERD cũ 27 bảng** theo lựa chọn của người dùng, trong khi CSDL có 37 bảng (V8, V9 thêm membership_tiers, tier_history, user_vouchers, gift_grants…). Khi người dùng muốn khớp thì chạy `/cineerd` rồi dựng lại.
- **Không dừng/kill `java.exe` hàng loạt** để tắt app 8081 (có thể tắt luôn app 8080 của IntelliJ); chỉ dừng đúng tiến trình đã tự khởi chạy (TaskStop).
- **Khối mã trong báo cáo** font Consolas 9; xuống dòng tay để mỗi dòng ≤ ~95 ký tự; chỉ trích đoạn thật, có ghi chú "rút gọn".
- **Chương I có hình minh họa như mẫu:** mục 1 Mô hình công nghệ chèn `hinh-ly-thuyet\client-server.png` ("Hình: Client - Server", rộng 12 cm, đặt ngay dưới tiêu đề mục); mục 4 MVC chèn `hinh-ly-thuyet\mvc.png` ("Mô hình MVC", rộng 11 cm, đặt sau dòng định nghĩa đầu tiên). Hai ảnh lấy từ mẫu, lưu trong skill; các lý thuyết khác (Java, Spring Boot…) không cần hình.

- Mọi con số, ngày, tên bảng, tên lớp, kết quả kiểm thử phải **có thật trong dự án** và đã kiểm chứng; theo CLAUDE.md: không ghi MoMo thật, SMTP, camera thiết bị thật, deploy là "đã xong" khi chưa thử. Chương V mô tả chạy local/JAR, không giả vờ đã deploy Internet.
- Số test lấy từ `target/surefire-reports` (0 skipped mới được nói "đủ bộ"); không cộng các lượt chạy lại.
- Pass/Fail chỉ ghi `Pass` khi test tự động hoặc lần thử tay đã xảy ra thật. Ca không chạy → bỏ hoặc ghi "Chưa kiểm thử".
- Bìa/Lời cảm ơn/Cam kết/Tên người: **không bịa**; để trống hoặc `<<>>` đến khi người dùng cung cấp.
- Tiếng Việt có dấu, văn phong ngắn gọn như mẫu; thuật ngữ kỹ thuật giữ nguyên tiếng Anh (Use case, Activity Diagram, Sitemap…). Không chép nguyên văn nội dung WellFitness (lý thuyết JavaScript/Next.js/JWT…) — viết lại cho đúng công nghệ CinePrime: Java, Spring Boot, MVC, MySQL, session/CSRF, MoMo.
- Không thêm file `.py`, script mới hay thư mục công cụ (AGENTS.md); mọi lệnh khác chạy inline.

## 4. Chỉnh trình bày

Chỉnh qua **cấu hình/cú pháp** chứ không sửa tay trong Word nếu muốn tái sinh được: đổi cỡ chữ đoạn bằng `::co n`; độ rộng cột bằng `||`; đổi tên đơn vị bằng `@to2:`. Người dùng có thể sửa trực tiếp trong Word sau bước cuối; nếu họ sửa tay rồi nhờ viết thêm, **đừng sinh đè** `.docx` — cập nhật `bao-cao.txt` rồi sinh ra file mới tên khác (`BaoCao-CinePrime-v2.docx`) và báo để họ chuyển nội dung.

## 5. Bảng soát

- [ ] Không còn `<<` hoặc `>>` chưa điền: `Select-String -Path 'Báo cáo\bao-cao.txt' -Pattern '<<' ` (dòng `>>` ký tên là cú pháp, bỏ qua).
- [ ] Không còn chữ đỏ `[THIẾU ẢNH` / `[THIẾU FILE` trong PDF soát.
- [ ] Bìa đủ khung viền, logo, tên đề tài; trang đầu không có chân trang; Lời cảm ơn là trang 1.
- [ ] Mục lục có đủ chương/mục, số trang khớp; Mục lục hình/bảng có mục; Hình/Bảng đánh số liên tục kể cả phần chèn từ Word ERD.
- [ ] Mỗi chương bắt đầu ở trang mới; không tiêu đề/ dòng dẫn mồ côi cuối trang; ảnh không tràn lề.
- [ ] Mã nguồn không chứa bí mật; số liệu kiểm thử khớp surefire; không khẳng định tính năng chưa kiểm chứng.
- [ ] Thư mục `Báo cáo\` chỉ còn `bao-cao.txt`, `Ảnh giao diện\`, `.docx` (đã xóa `.pdf`/tạm).
