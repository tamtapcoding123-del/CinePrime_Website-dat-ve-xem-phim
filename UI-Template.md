# UI-TEMPLATE — CINEPRIME

> **Nguồn cảm hứng:** [Galaxy Cinema](https://www.galaxycine.vn/) — tham khảo cấu trúc trải nghiệm đặt vé và tổ chức nội dung, kiểm tra ngày 05/11/2026.  
> **Phạm vi:** Bộ thiết kế giao diện tham khảo cho website **CinePrime – Hệ thống đặt vé xem phim**. Không sử dụng logo, font độc quyền, mã nguồn hoặc dữ liệu phim thực của Galaxy Cinema.  
> **Trạng thái:** Có bản demo HTML/CSS/JS tương tác; **chưa có backend hoặc thanh toán thật**.

## 1. Mục tiêu giao diện

Thiết kế website đặt vé xem phim rõ ràng, chuyên nghiệp, ưu tiên **đặt vé nhanh**, giúp khách hàng đi theo luồng **Chọn phim → Chọn rạp → Chọn ngày → Chọn suất → Chọn ghế → Thanh toán → Nhận vé**. Áp dụng phong cách điện ảnh hiện đại: nền trắng làm nội dung chính, thanh điều hướng xanh đen, xanh dương nhận diện và cam cho nút hành động.

### Nguyên tắc

1. CTA đặt vé là ưu tiên số 1, dùng màu cam nhất quán.
2. Luồng đặt vé có thứ tự rõ và xác nhận lại thông tin cuối cùng.
3. Card phim tập trung poster; tên, tuổi, điểm, thể loại, thời lượng dễ đọc.
4. Không dùng màu sắc gây rối; tối ưu khoảng trắng và lưới 12 cột.
5. Giao diện khách responsive, dễ dùng trên điện thoại, tránh thao tác hover-only. Admin/Staff chỉ cần desktop (từ 12/11/2026).
6. Đáp ứng trạng thái tải, rỗng, lỗi, không có suất và ghế không còn.

## 2. Cấu trúc website tham khảo

### 2.1. Khung trang chủ

```text
┌─────────────────────────────────────────────────────────────────┐
│ Top Bar: thông báo chương trình thành viên / hỗ trợ            │
├─────────────────────────────────────────────────────────────────┤
│ Logo CinePrime     Tìm kiếm                    Vé   Tài khoản   │
├─────────────────────────────────────────────────────────────────┤
│ MUA VÉ | PHIM | LỊCH CHIẾU | KHUYẾN MÃI | GÓC ĐIỆN ẢNH | RẠP │
├─────────────────────────────────────────────────────────────────┤
│ Banner phim lớn / Hero Carousel            [CTA Đặt vé]       │
├─────────────────────────────────────────────────────────────────┤
│ MUA VÉ NHANH | Chọn phim | Chọn rạp | Chọn ngày | Suất | [MUA]│
├─────────────────────────────────────────────────────────────────┤
│ PHIM [Đang chiếu] [Sắp chiếu] [IMAX]         [Chọn khu vực]     │
│ [Poster 1] [Poster 2] [Poster 3] [Poster 4]                     │
│ Tên phim + tuổi + rating + thời lượng     [Xem thêm]           │
├─────────────────────────────────────────────────────────────────┤
│ LỊCH CHIẾU  [Ngày 1] [Ngày 2] ... [Ngày 7]                     │
│ Từng phim + các suất giờ đặt vé                             │
├─────────────────────────────────────────────────────────────────┤
│ Lợi ích đặt vé / ưu đãi thành viên                              │
├─────────────────────────────────────────────────────────────────┤
│ Khuyến mãi: banner 3 cột                                       │
├─────────────────────────────────────────────────────────────────┤
│ Góc điện ảnh: tin tức / cẩm nang / bình luận phim              │
├─────────────────────────────────────────────────────────────────┤
│ Footer: Giới thiệu - Hỗ trợ - Liên hệ - Điều khoản             │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2. Các màn hình cần thiết cho dự án hoàn chỉnh

| Màn hình | Thành phần | Chức năng | Ưu tiên |
|---|---|---|---|
| Trang chủ | Header, hero, mua vé nhanh, phim, lịch chiếu, khuyến mãi, footer | Khám phá và bắt đầu mua vé | P0 |
| Danh sách phim | Bộ lọc Đang chiếu/Sắp chiếu, tìm kiếm, card | Tìm phim | P0 |
| Chi tiết phim | Poster, tóm tắt, trailer, thể loại, thời lượng, tuổi, suất chiếu | Chọn lịch xem | P0 |
| Danh sách rạp | Vị trí, địa chỉ, suất chiếu, giá tham khảo | Chọn rạp | P0 |
| Đặt vé – Chọn suất | Ngày, rạp, phòng, loại định dạng, các giờ | Chọn suất | P0 |
| Đặt vé – Chọn ghế | Sơ đồ ghế, chú giải, giá từng loại, tổng tiền | Chọn ghế | P0 |
| Đặt vé – Combo | Combo bắp/nước, số lượng, giá | Upsell | P1 |
| Thanh toán | Đơn hàng, mã giảm giá, phương thức thanh toán | Xác nhận thanh toán | P0 |
| Xác nhận vé | Mã đơn, QR vé, phim/rạp/giờ/ghế | Nhận vé | P0 |
| Tài khoản | Đăng ký, đăng nhập, quên mật khẩu | Xác thực | P0 |
| Hồ sơ của tôi | Vé đã mua, lịch sử, đổi mật khẩu | Quản lý cá nhân | P1 |
| Khuyến mãi | Danh sách banner, chi tiết ưu đãi | Marketing | P1 |
| Admin Dashboard | Phim, rạp, phòng, ghế, suất, giá, đơn hàng, user, báo cáo | Vận hành | P0 |
| Staff Dashboard | Soát vé QR, kiểm tra suất, xác nhận check-in | Vận hành tại rạp | P1 |

> **Demo đi kèm hiện hỗ trợ:** giao diện trang chủ, tab phim, modal chi tiết, lịch chiếu, chọn ghế, 2 bước thanh toán minh họa, form tài khoản. Các màn hình khác là yêu cầu thiết kế cho bản triển khai Spring Boot.

## 3. Design System

> **Từ 15/11/2026:** màu, phông và hình khối theo mục **3.5 "Studio Cinema Pearl"**. Các giá trị màu, bo góc và phông ghi ở 3.0–3.4 là của giao diện xanh–cam cũ, chỉ giữ để tra cứu; phần **bố cục, mật độ, luồng và nghĩa màu trạng thái** của 3.0 và 3.4 vẫn có hiệu lực.

### 3.0. Giao diện khách (từ 12/11/2026)

Trang khách dùng `static/css/site.css`; token khai báo ở `:root` đầu file. Admin/Staff theo mục **3.4** (phương án B, chốt 13/11/2026); bảng 3.1–3.3 là token cũ của `app.css`/`operations.css`, chỉ giữ để tra cứu cho tới khi 3.4 được triển khai.

| Token | Giá trị | Vai trò |
|---|---|---|
| `--bg` / `--bg-soft` | `#FFFFFF` / `#F5F6F8` | Nền trang / nền phân khu |
| `--ink` / `--muted` | `#14171C` / `#5B6472` | Chữ chính / chữ phụ |
| `--line` / `--line-strong` | `#E3E6EA` / `#CFD4DA` | Viền thẻ / viền ô nhập, nút phụ |
| `--navy` | `#111827` | Thanh menu, footer, dải thành viên, trạng thái đang chọn |
| `--accent` / `--accent-hover` | `#F47B20` / `#DD6A12` | Màu nhấn duy nhất: nút hành động chính, ghế đang chọn |
| `--accent-ink` | `#B4530B` | Chữ cam trên nền trắng (đủ tương phản) |

- Font: **Be Vietnam Pro** (OFL, tự host trong `static/fonts`, 400/500/600/700, tách bộ ký tự Latin / Latin mở rộng / tiếng Việt) → `Segoe UI` → `Arial`.
- Cỡ chữ: nội dung 16px/1.6; chú thích 14px; tên phim 17px/600; tiêu đề khối 28px/700 (mobile 22px); tiêu đề trang 36px/700 (mobile 28px); tiêu đề banner `clamp(32px, 4vw, 56px)`/700; nút 15px/600. Chữ thường theo câu, không viết hoa toàn bộ.
- Hình khối: bo góc 8px; viền 1px thay đổ bóng (chỉ hộp thoại có bóng); không gradient trang trí (chỉ lớp tối dưới chữ của banner); không ký tự trang trí.
- Khoảng cách: khối 72px (850px: 56px, 620px: 48px); vùng chạm tối thiểu 44px.
- Ảnh: chỉ ảnh thật do Admin tải lên. Ưu đãi/bài viết không có ảnh hiển thị thẻ chữ (loại, tiêu đề, trích đoạn, ngày); ảnh thẻ khuyến nghị 1200×675 (16:9).

**Bố cục gọn theo phác thảo (chốt 13/11/2026, đã triển khai mã):** canvas "CinePrime Admin/Staff — phác thảo", trang "Trang khách — theo phác thảo (13/11)". Mục tiêu: ở 1366×768 thấy trọn header + hero + mua vé nhanh + đầu hàng phim.

| Token mới | Giá trị | Vai trò |
|---|---|---|
| `--brand` / `--brand-soft` | `#1459A5` / `#E8F1FB` | Chữ "Cine" ở logo, liên kết "Xem tất cả →", mục menu đang mở trên mobile, ô chữ cái tài khoản |
| `--container` | `1328px` | 1280px nội dung + 2 lề 24px |
| `--header-h` | `64px` (mobile `56px`) | Header một hàng, dính đầu trang |
| `--hero-h` | `clamp(300px, 46svh, 400px)` | 300px ở laptop 1366×768, 400px ở màn lớn |
| `--section` | `56px` (≤1440 rộng hoặc ≤820 cao: `48px`; ≤850: `40px`) | Nhịp giữa các khối |

- Cỡ chữ: h1 32px (laptop 30px, mobile 28px); h2 24px (laptop/mobile 22px). Nút cam dùng **chữ đậm `--ink`** (chữ trắng trên `#F47B20` dưới 3:1).
- **Header:** một hàng: logo · Trang chủ · Phim ▾ (Đang chiếu, Sắp chiếu, Lịch chiếu) · Góc điện ảnh · Hệ thống rạp · Khuyến mãi · kính lúp (mở dòng tìm kiếm dưới header) · Đăng nhập (viền, bo 19px) + Đăng ký (xanh đen). Đã đăng nhập: nút tài khoản (chữ cái đầu + tên) mở menu Vé của tôi / Phim yêu thích / Điểm thưởng / Quản trị hoặc Soát vé / Đăng xuất. Menu xổ bằng click/Enter, đóng bằng Escape hoặc bấm ngoài. Bỏ dải chữ trên cùng và thanh menu xanh đen; thông báo (NOTICE) hiện thành dải cam nhạt mỏng, đóng được. Không có mục "Sự kiện". ≤850px: logo · nút menu; menu là tấm trắng toàn màn (ô tìm kiếm, mục 52px, nhóm Phim xổ tại chỗ, Đăng nhập/Đăng ký ở cuối), khoá cuộn nền.
- **Hero:** slide phim = cột chữ (chip định dạng/tuổi/trạng thái, tên phim, thể loại · thời lượng · ngôn ngữ có icon, mô tả 2 dòng, nút cam "Mua vé ngay"/"Đặt trước" + "Xem trailer", "Đạo diễn: …") + ảnh ngang 16:9 + poster 2:3 (poster ẩn ≤1090). Định dạng/ngôn ngữ lấy giá trị phổ biến nhất của các suất sắp tới, không có suất thì ẩn. Slide khuyến mãi vẫn chỉ ảnh, nằm giữa nền xanh đen. Một slide mỗi lúc (không ló hai bên). Điều khiển: thanh trắng giữa đáy gồm ‹ chấm · tạm dừng · "1 / 3" ›; >1440px mũi tên tròn 40px ra hai mép. ≤850: ảnh ngang trên, chữ dưới, nút chia 2 cột, vuốt để chuyển.
- **Mua vé nhanh:** dải trắng sát dưới hero, rộng bằng nội dung: 4 ô (số trong vòng tròn xanh đen, bước đang cần chọn số cam) + nút cam "Mua vé" 200px. ≤1090: ô có viền 2×2, nút cả hàng; ≤620: phim, rạp cả hàng, ngày | suất.
- **Khối phim:** tiêu đề có vạch trái 3px xanh đen + dòng phụ; tab nhỏ (đang chọn nền xanh đen); "Xem tất cả →" xanh. Lưới 6 cột (≤1280: 5, ≤1090: 4, ≤850: 3, ≤620: 2), khoảng 28/16px. Thẻ: poster 2:3, nhãn tuổi, tên 15px/600 tối đa 2 dòng, thể loại · thời lượng 13px, tim (thích; chưa đăng nhập thì dẫn tới Đăng nhập). Chuột: "Mua vé"/"Xem chi tiết" nổi trên chân poster khi rê hoặc focus; màn cảm ứng: nút chính luôn hiện dưới thẻ.
- **Footer:** nền `--bg-soft`, 5 cột: logo + mô tả · Về chúng tôi · Hỗ trợ khách hàng · Thành viên CinePrime · Liên hệ. Liên kết lấy từ nội dung ABOUT/POLICY/CONTACT đang bật (không có thì ghi "đang được cập nhật"), không ghi hotline/email giả, không icon mạng xã hội khi chưa có liên kết thật.

**Hero (banner trang chủ):** quản lý tại Admin → Nội dung → Banner trang chủ. Cả slide khuyến mãi là link tới liên kết khuyến mãi; slide phim dẫn tới trang phim (tên, ảnh, poster, nút). Phim chưa có ảnh ngang dùng được không lên banner (Admin báo "Thiếu ảnh ngang"). Ảnh banner khuyến nghị 1920×640, chia ba mức và không bao giờ phóng to ảnh: rộng ≥ 1600px và tỉ lệ ≥ 16:9 thì phủ kín khung; ảnh ngang nhỏ hơn (rộng ≥ 560px, tỉ lệ ≥ 4:3) đặt giữa khung trên nền tối, đúng kích thước (tải lên được nhưng có cảnh báo). Ảnh dọc hoặc hẹp hơn 560px không nhận làm banner. *(Bản 12/11 "chỉ ảnh, slide 3:1 ló hai bên" đã thay bằng bố cục trên.)*

**Trang chi tiết phim:** dải đen đầu trang (cao 240–480px) đặt ảnh ngang của phim ở giữa, không phóng to, nút play tròn mở trailer; poster viền trắng đè lên mép dải; bên phải poster: breadcrumb, tên phim + nhãn tuổi, thời lượng/ngày khởi chiếu/điểm có icon, Thể loại/Đạo diễn/Diễn viên dạng chip, khoảng chiếu, nút hành động. Dưới: Nội dung phim, Lịch chiếu, Khán giả chia sẻ. Cột phải 320px "Phim đang chiếu" (thẻ ảnh ngang 16:9, điểm, nhãn tuổi); dưới 1090px cột này xuống cuối trang. Phim chưa có ảnh ngang thì không có dải đen.

Tự chuyển 5–10 giây (Admin chỉnh, mặc định 6), có chấm vị trí, mũi tên, nút tạm dừng, vuốt trên mobile; dừng khi rê chuột, khi focus bằng bàn phím, khi tab ẩn và khi hệ điều hành bật giảm chuyển động. Mobile: ảnh ở trên, chữ và nút ở dưới.

### 3.1. Bảng màu (design tokens)

| Token | Giá trị HEX | Vai trò |
|---|---|---|
| `--blue` | `#1459A5` | Nhận diện, tab active, liên kết |
| `--blue-2` | `#1A68BA` | Hover/secondary blue |
| `--orange` | `#F47B20` | CTA đặt vé, điểm nhấn, ghế đã chọn |
| `--orange-hover` | `#DC6811` | CTA hover |
| `--navy` | `#111827` | Menu/header tối |
| `--ink` | `#1E293B` | Chữ nội dung chính |
| `--muted` | `#788395` | Chữ hỗ trợ |
| `--line` | `#E8EDF3` | Viền/divider |
| `--soft` | `#F6F8FB` | Nền phân khu |
| `--surface` | `#FFFFFF` | Card/form |
| `--success` | `#279568` | Thành công |
| `--danger` | `#E34D46` | Mức phân loại T18 / lỗi |

*Không nên dùng đỏ và vàng làm gam chủ đạo. Tỷ lệ khuyến nghị:* nền trắng/xám nhạt 70–75%; xanh đen/xanh dương 20–25%; cam 5–10% (CTA, trạng thái chọn).

### 3.2. Typography

- Font: `Inter` → `Segoe UI` → `Arial` → `sans-serif`.
- Hero H1: 40–63 px, weight 800–850.
- H2 section: 22–25 px, weight 800.
- Card title: 13–16 px, weight 800.
- Body: 13–15 px, line-height 1.5–1.7.
- Metadata: 10–12 px, màu muted.
- Không sử dụng quá 3 kiểu weight trong cùng thành phần.

### 3.3. Spacing, grid và responsive

Phạm vi (12/11/2026): các quy tắc mobile/tablet/breakpoint dưới đây chỉ áp dụng cho giao diện khách. Màn Admin/Staff thiết kế và nghiệm thu cho desktop (1366×768 trở lên).

- Content max-width **1232px**, padding hai bên **24px** desktop / **16px** mobile.
- Grid phim: desktop **4 cột**, tablet **3 cột**, mobile **2 cột**.
- Poster: tỷ lệ **2:3**, bo góc **9px**.
- Section spacing: **68px** desktop / **50px** mobile.
- Khoảng cách card: ngang **21px**, dọc **29px**.
- Shadow: nhẹ, tập trung banner mua vé nhanh và modal.
- Breakpoints: `1090`, `850`, `620` px.
- Nút: bán kính **7px**, mục tiêu chạm trên điện thoại gần 44px.

### 3.4. Giao diện vận hành Admin/Staff — "Thẻ màu nhạt" (chốt 13/11/2026, phương án B)

> **Trạng thái:** người dùng chọn phương án B ngày 13/11/2026, thay hướng "Sổ suất chiếu" đen trắng (12/11) vì bản đó nhàm và khó quét mắt. **Đã triển khai 13/11/2026:** Admin — `static/css/admin-sheet.css` viết lại theo B (nạp sau cùng: nền `--canvas`, thẻ, chip vuông theo `tone-*`, thẻ số liệu có vạch + ô icon, sidebar hiện icon `navIcon`, nút xanh/cam); Tổng quan có ô icon và màu nghĩa cho 4 thẻ số liệu, số 0 hiện `0 đ` thay cho "—"; Đơn hàng hiện số 0 thật; nút "+ Thêm …" là nút cam (`.button.primary`), "Lọc" là nút xanh; trạng thái Đã hủy/Thất bại dùng tone `bad` (đỏ); cột mã bản ghi đổi thành `#` (hết trùng "Mã | Mã" ở Voucher); mức giảm voucher có đơn vị (`100%`, `20.000 đ`); breadcrumb có khoảng trắng. Staff — lớp màu B ở cuối `static/css/staff.css` (bố cục và luồng Soát vé/Bán quầy giữ nguyên): chip trạng thái suất và vé, thẻ kết quả soát tô nền theo kết quả, dải số thành thẻ có vạch, dải ca màu quầy, ghế VIP tím/Đôi cam, chú giải ghế thêm Thường/VIP/Đôi. Đã xem trên bản thử cổng 8084 (`cineprime_test`, 1440×900): Tổng quan, Đơn hàng, Phim, Voucher, Soát vé, Bán vé tại quầy. Chưa làm: trục giờ "Hôm nay" ở Tổng quan và Soát vé; các trình biên tập riêng (sơ đồ ghế, form phim chi tiết) chưa soát lại theo B. Nhóm Lịch chiếu đã làm lại theo B ngày 14/11/2026 (xem "Lịch chiếu" ở Màn chính). Chỉ desktop ≥ 1366×768. Skill dự án duy nhất: `/cineuxui <màn>` (`.claude/skills/cineuxui`) — quy trình phác thảo, sửa và soát UI cho cả khách lẫn vận hành. Bản phác thảo: canvas riêng tư "CinePrime Admin/Staff — phác thảo" trên claude.ai, trang "Bản 2 — Có màu" (bảng màu/chip, phương án A · B · C trên màn Đơn hàng).

Màu có nghĩa, không màu trang trí: mỗi màu gắn một ý nghĩa cố định trên mọi màn, nhìn là biết trạng thái, kênh bán, khu chức năng. Nền trang xám rất nhạt, khối là thẻ trắng viền 1px, ô số liệu có vạch màu và ô icon nhạt. Không gradient, không emoji, không bóng đổ trên thẻ.

**Nguyên tắc**

1. **Màu = nghĩa.** Dùng đúng bảng ngữ nghĩa bên dưới; không tô màu chỉ để đẹp. Mỗi chip luôn có chữ, không chỉ dựa vào màu.
2. **Thẻ trắng trên nền nhạt.** Khối là thẻ (`--card`, viền `--line`, bo 10px) trên nền `--canvas`; bảng, bộ lọc và tab của cùng một danh sách nằm chung một thẻ.
3. **Trạng thái là chip vuông nhạt.** "Đang hoạt động", "Đã thanh toán"… là ô bo 4px, nền nhạt + chữ đậm cùng tông + viền 1px. Không dùng ô 8px trơn, không pill tròn.
4. **Cam để tạo/bán, xanh để làm việc.** Nút cam (chữ `--ink`) cho hành động tạo hoặc bán, tối đa một mỗi màn; nút xanh cho lọc/xác nhận/lưu thường; liên kết, tab và mục đang chọn màu xanh.
5. **Số rõ ràng.** Chữ số tabular, cột tiền căn phải, tiền `170.000 đ`, phần trăm `100%`; giá trị 0 hiện `0` kèm dòng giải thích ngắn (không dải "—" lặp lại).
6. **Rỗng là một dòng.** Nói thẳng chuyện gì đang xảy ra và việc tiếp theo; nút chưa đủ điều kiện thì thay bằng dòng hướng dẫn, không để nút mờ.

**Token nền** (khai báo ở `:root`, không dùng hex rời trong rule)

| Token | Giá trị | Vai trò |
|---|---|---|
| `--canvas` | `#F4F6F9` | Nền vùng nội dung |
| `--card` | `#FFFFFF` | Thẻ, thanh đầu, sidebar |
| `--card-head` | `#F8F9FB` | Nền đầu bảng |
| `--ink` / `--ink-2` / `--muted` | `#14171C` / `#3D434D` / `#5B6472` | Chữ chính / chữ phụ dài / nhãn, chú thích |
| `--line` / `--line-soft` / `--line-strong` | `#E3E6EA` / `#EEF0F3` / `#CFD4DA` | Viền thẻ / kẻ dòng trong bảng / viền ô nhập, nút phụ |
| `--brand` / `--brand-soft` | `#1459A5` / `#E8F1FB` | Liên kết, tab, mục sidebar đang mở, nút xanh / nền mục đang mở |
| `--accent` / `--accent-ink` | `#F47B20` / `#A9480A` | Nút tạo/bán (chữ `--ink`), ghế đang chọn / chữ cam trên nền trắng |

**Màu ngữ nghĩa** (chip = nền / chữ / viền; vạch = vạch đầu thẻ số liệu)

| Nghĩa | Nền | Chữ | Viền | Vạch | Dùng cho |
|---|---|---|---|---|---|
| Ổn (`ok`) | `#E6F4EA` | `#1B6B3F` | `#C5E6CF` | `#1F8A4C` | Đang hoạt động · Đang chiếu · Đang hiển thị · Đã thanh toán · Đã vào · Hợp lệ |
| Thông tin (`info`) | `#E8F1FB` | `#1459A5` | `#C9DDF4` | `#1459A5` | Sắp chiếu · Đang giữ ghế · Chờ MoMo · Mở bán · Online |
| Cần xem (`warn`) | `#FFF5D6` | `#7A5300` | `#F1DE9E` | `#D9A400` | Chờ đối soát · Sắp hết hạn · Thiếu ảnh ngang · Vé đã dùng |
| Lỗi (`bad`) | `#FDECEC` | `#B42318` | `#F6CACA` | `#C2362F` | Đã huỷ · Thanh toán lỗi · Sai suất · Không thuộc rạp · Đã khoá |
| Tắt (`off`) | `#F1F2F4` | `#4B5563` | `#E0E3E8` | `#9AA2AD` | Đã tắt · Đang ẩn · Hết hạn · Đã kết thúc · Nháp · Chưa vào |
| Quầy (`counter`) | `#FEF0E4` | `#A9480A` | `#F9D4B4` | `#E06A12` | Tại quầy · ghế Đôi |
| Đặc biệt (`special`) | `#F1ECFB` | `#5B3BA8` | `#DDD1F4` | `#6B4BC2` | ghế VIP · Voucher |
| Bắp nước (`food`) | `#E3F4F4` | `#0F6466` | `#BFE3E3` | `#0F7C7E` | Combo · Đồ uống |

Nhãn tuổi giống trang khách, nền đặc chữ trắng 11/700: P `#1F8A4C` · K `#1459A5` · T13 `#B7791F` · T16 `#D9620C` · T18 `#C2362F`. Thể loại luôn là chip `off` chữ 500.

**Icon theo mục sidebar** (sidebar phẳng kiểu ứng dụng chat, người dùng chốt 13/11/2026, thay cho nhóm đóng/mở): mỗi mục có một icon nét riêng, mixin `navIcon(id)` trong `templates/admin/layout.pug` (khung 24×24, 20px, nét 1.6, không tô nền, không màu riêng từng mục):

| Mục (`id`) | Hình |
|---|---|
| Tổng quan (`overview`) | 4 ô chữ nhật lệch cao |
| Đơn hàng (`orders`) | hoá đơn răng cưa đáy + 3 dòng |
| Phim (`movies`) | cuộn phim |
| Lịch chiếu (`schedule`) | khung lịch + 2 khoen + dòng |
| Rạp & phòng (`cinemas`) | toà nhà mái nhọn + cửa |
| Ca tại quầy (`shifts`) | hai người |
| Kinh doanh (`business`) | thẻ giá |
| Nội dung (`content`) | khung ảnh |
| Hệ thống (`system`) | bánh răng |

Icon màu `--muted`; mục đang mở nền `--brand-soft`, icon và chữ `--brand` 600.

**Chữ:** Be Vietnam Pro (đã tự host). Thang cỡ: 12 nhãn/chip · 13 bảng · 14 nội dung, ô nhập, sidebar · 16/600 tiêu đề thẻ · 28/700 tiêu đề trang · 26–28/700 số trong thẻ số liệu. Mã vé/mã đơn dùng `ui-monospace, Consolas, monospace` 600. Viết hoa đầu câu.

**Hình khối và khoảng cách:** thẻ bo 10px; ô số liệu, nút, ô nhập bo 6px; chip bo 4px; ô icon 36px bo 8px; ghế bo 3px. Lưới bội số 4px; khoảng cách giữa các thẻ 12–16px; đệm thẻ 14–16px. Dòng bảng ≥ 44px, ô nhập/nút 38px, chip 24px. Bóng chỉ cho hộp thoại và menu nổi: `0 8px 24px rgba(20,23,28,.16)`. Focus: viền `--brand` 2px cách 2px.

**Khung**

- **Thanh trên cùng** trắng, kẻ dưới `--line`. **Cụm đầu trái giữ nguyên như hiện tại** (người dùng chốt): nút ẩn/hiện sidebar, mũi tên lùi/tiến, logo `cineprime-mark.svg` + "Cine" xanh "Prime" cam. Bên phải (Admin, chốt 13/11/2026): chỉ nút viền "Xem website" có icon mở tab mới; tên người dùng (ô tròn chữ cái đầu + tên + vai trò) và "Đăng xuất" (một dòng như mục sidebar, rê chuột tô `--bad-bg`) nằm ở đáy sidebar, kẻ trên `--line`. Staff giữ nguyên tên + "Đăng xuất" trên thanh đầu.
- **Admin:** sidebar trắng 232px, kẻ phải, thu gọn được (Ctrl+B). **Danh sách phẳng, không nhóm đóng/mở** (chốt 13/11/2026): mỗi mục cao 36px = icon 20px + tên 14/500, cách 12px; nhãn mục "Vận hành", "Quản lý" là chữ 12/500 `--muted`, không bấm được. Chín mục theo thứ tự: Tổng quan · Đơn hàng · *Vận hành:* Phim · Lịch chiếu · Rạp & phòng · Ca tại quầy · *Quản lý:* Kinh doanh · Nội dung · Hệ thống — định nghĩa ở `NAV` trong `AdminController`. Nội dung đệm 24/32px trên nền `--canvas`.
- **Trang gộp có tab** (`.hub-head` + `.hub-tabs`): mục có nhiều màn mở ra tiêu đề khu (28/700) và **dải tab phân đoạn** (người dùng yêu cầu thêm vách ngăn, nổi bật hơn — 13/11/2026): thẻ trắng viền `--line-strong` bo 8px, mỗi tab cao 42px đệm ngang 20px, chữ 14/600 `--ink-2`, ngăn nhau bằng vạch dọc 1px; tab đang mở nền `--brand-soft`, chữ `--brand`, gạch dưới 3px `--brand`. Tab kênh ở Đơn hàng (`.channel-tabs`) dùng cùng kiểu. Mỗi tab giữ URL riêng; tab theo rạp mang theo `cinemaId`. Tab chỉ hiện ở màn danh sách, không ở form sửa. Lịch chiếu không dùng tab nữa (nhóm sidebar Theo ngày · Danh sách · Xếp lịch tuần, 14/11/2026); Rạp & phòng = Rạp · Phòng · Ghế / Bảo trì; Kinh doanh = Bảng giá · Bắp nước / Combo · Voucher; Nội dung = Banner trang chủ · Bài viết / Ưu đãi / Chính sách · Bình luận; Hệ thống = Người dùng · Nhật ký hoạt động. Khi có tab, trang không in lại h1 riêng; nút tạo ghi rõ đối tượng ("+ Thêm voucher").
  **Đầu trang một hàng (16/11/2026):** trang có tab bỏ breadcrumb; tiêu đề khu (22/700), dải tab, rồi bên phải số kết quả + nút tạo (`.hub-tools`) nằm cùng một hàng.
  **Rạp & phòng (16/11/2026):** bộ lọc nằm một dòng, nhãn đứng cạnh ô (`.ops-filters.is-inline`, ô cao 36px, tự xuống dòng khi quá 4 ô như tab Ghế). Bỏ hộp "Rạp đang quản lý" và không có dòng đường dẫn (người dùng bỏ 16/11/2026; bộ lọc Rạp đã nói rõ). Liên kết bậc dưới nằm ngay ở số đếm: cột "Phòng hoạt động" ở tab Rạp ("2 phòng →") mở tab Phòng của rạp đó; cột "Ghế hoạt động" ở tab Phòng ("60 ghế →") mở tab Ghế của phòng đó. Tab mang theo `cinemaId` (và `roomId` cho tab Ghế). Thẻ danh sách vừa cửa sổ (`.panel.hub-fit`): trang không cuộn, chỉ bảng cuộn trong thẻ, đầu bảng dính.
  **Thêm/Sửa phòng (16/11/2026):** "+ Thêm phòng" mở pop-up (rạp lấy theo bộ lọc; Tên, Định dạng, Số hàng ghế mặc định 8) → nút cam "Tạo và xếp ghế →" vào thẳng màn sơ đồ, mẫu đã dựng sẵn (chưa lưu). "+ Phòng" ở tab Rạp và "Lưu và tạo phòng" mở cùng pop-up (`?add=1`). Sửa phòng cũng là pop-up, có nút "Sơ đồ ghế →". Màn thiết lập có thanh bước ✓ Thông tin (mở lại pop-up) · 2 Sơ đồ ghế · 3 Giá vé.
  **Mẫu phòng mặc định:** 4 ghế · lối đi giữa · 4 ghế; hai hàng cuối toàn ghế đôi (mỗi bên 2 cặp); không VIP. 4 ghế/bên chỉ là mẫu, không phải giới hạn (vẫn 20 hàng, 30 cột, 600 ghế). Hộp "Mẫu phòng" có Nhỏ 6 hàng · 3|3, Chuẩn 8 hàng · 4|4, Lớn 12 hàng · 4|4 và các ô số hàng, ghế mỗi bên, số hàng ghế đôi (0–2), lối đi, VIP.
  **Màn Sơ đồ ghế (16/11/2026, lần 2):** công cụ nằm một thanh ngang trên sơ đồ (Thường · VIP · Ghế đôi · Bảo trì · Xóa ghế · Xem / đổi tên | ↶ ↷ | Mẫu phòng · Xem 3D), không còn cột trái và không còn nút +/− Hàng/Cột; thu phóng nổi ở góc dưới phải sơ đồ; bỏ dòng đếm lặp (Tổng quan bên phải đã có) và nút Hoàn tất trên đầu. Nút "+" quanh sơ đồ (nét đứt, hiện khi rê chuột): hai đầu mỗi hàng = thêm 1 ghế vào hàng đó (hàng ghế đôi thêm cả cặp; mép trái tự dời cả phòng); cạnh trên chữ hàng = chèn hàng (chép hàng đó); cạnh trái số cột = chèn cột; hai đầu dãy số cột = thêm cột ở mép; "+ Thêm hàng" dưới cùng. "Lối đi (bỏ ghế)" đổi thành "Xóa ghế"; phím Delete xóa ghế đang chọn.
  **Màu ghế trong sơ đồ Admin (16/11/2026):** Thường = nền `--card` viền `--line-strong`; VIP = `--warn-bg` viền `--warn-bar` chữ `--warn-fg`; Ghế đôi = `--special-bg` viền `--special-bar` chữ `--special-fg`; Bảo trì = nền `--muted` đặc, chữ trắng gạch ngang (không kẻ sọc). Nút công cụ, chú thích và menu hàng dùng đúng các màu này. Tình trạng bán vé: Đang giữ `--info-line`, Đã bán `--ink-2`. Trang khách giữ màu riêng.
  **Tab Ghế / Bảo trì:** chưa chọn phòng thì liệt kê ghế đang bảo trì của mọi phòng trong rạp (có cột Phòng, bộ lọc Hoạt động = "Bảo trì"); chọn phòng thì hiện toàn bộ ghế của phòng.
  **Bước Giá vé (16/11/2026):** một bảng duy nhất, sửa tại chỗ: hàng = loại ghế (loại phòng chưa dùng vẫn hiện, ghi mờ "Phòng này chưa có …"), cột = Thứ 2–6 / Thứ 7, CN. Mỗi ô liệt kê khung giờ + ô số tiền (Enter lưu, Esc bỏ); ô trống "+ Nhập giá"; giờ hở hiện "Thiếu giá hh:mm–hh:mm" kèm ô nhập. "+ Chia khung giờ" tách theo giờ nhập; mốc giữa hai khung sửa ngay trên dòng; × gộp vào khung bên cạnh. Nút "Chép giá Thứ 2–6 sang Thứ 7, CN" (có xác nhận). Cột phải chỉ là hướng dẫn ngắn; bỏ danh sách, bộ lọc, Trước/Sau/Đọc lại. Bước cuối: "Hoàn tất" là nút chính, ẩn "Kế tiếp".
  **Sửa sơ đồ:** trên lưới có số cột; bấm chữ hàng → menu Đổi cả hàng thành (Thường/VIP/Đôi/Bảo trì) · Chèn hàng phía trên/dưới (chép ghế của hàng đó) · Xóa hàng; bấm số cột → Chèn cột trái/phải · Biến thành lối đi · Xóa cột. Xóa hỏi xác nhận, hoàn tác được; ghế đã có lịch sử bán vé không bị bỏ. Tên ghế đánh liền 1, 2, 3… theo hàng, bỏ qua lối đi (A1–A4 | A5–A8); tên gõ tay được giữ.
  **Nhóm sidebar Lịch chiếu nhớ trạng thái mở/đóng** (16/11/2026, `admin-nav-groups.js`, lưu trong trình duyệt): đổi sang màn khác không tự đóng nhóm.
- **Staff:** không sidebar; hai tab "Soát vé" · "Bán vé tại quầy" trên thanh đầu (tab đang mở chữ `--brand` 600 + gạch dưới 2px). Tên rạp phụ trách in đậm bên phải. Nút ẩn/hiện ở cụm trái thu gọn cột danh sách suất.

**Thành phần**

- **Đầu trang:** breadcrumb (`Quản trị / Đơn hàng`, có khoảng trắng hai bên dấu `/`) → tiêu đề 28/700 (không kèm icon hay ô màu) → một dòng mô tả `--muted`; nút bên phải (cam nếu là tạo mới: "+ Thêm phim", "+ Thêm banner").
- **Thẻ số liệu:** 4 thẻ một hàng, vạch trên 3px theo nghĩa, ô icon 36px nền nhạt cùng nghĩa (icon nét 1.8px), nhãn 12px, số 26–28/700, một dòng phụ tô chữ cùng nghĩa. Thẻ trung tính (tổng, khớp bộ lọc) dùng `info`.
- **Thẻ danh sách:** trong một thẻ: tab kênh (tab đang mở có chip đếm `info`) → hàng bộ lọc (nhãn trên ô, nút "Lọc" xanh) → bảng (đầu bảng nền `--card-head`, kẻ `--line-soft`) → dòng phân trang. Cột trạng thái, kênh, loại dùng chip.
- **Hành động trong dòng:** "Sửa" nền `--brand-soft` chữ `--brand`; "Xoá" nền trắng viền đỏ nhạt chữ `bad`; các hành động phụ trong menu "⋯".
- **Danh sách "Cần để ý":** mỗi dòng chip nghĩa + tiêu đề 600 + dòng chi tiết + liên kết; không có việc thì một dòng với chip `ok`.
- **Trục giờ "Hôm nay"** (Tổng quan, Soát vé): hàng theo phòng, thước giờ; mỗi suất là thanh nền `info` nhạt, phần đậm = tỉ lệ lấp đầy, suất đang chiếu viền `--brand` 2px; vạch "bây giờ" cam 2px kèm nhãn giờ `--accent-ink`.
- **Ghế:** Trống viền `--line-strong`; Thường/VIP/Đôi theo `off`/`special`/`counter` khi cần phân loại; Đang chọn nền `--accent` chữ `--ink`; Quầy này giữ nền `--brand` chữ trắng; Đã bán / nơi khác giữ nền `#CFD4DA`; Bảo trì viền nét đứt.

**Màn chính**

- **Tổng quan (Admin):** đầu trang + bộ lọc kỳ/ngày/rạp + "Xuất CSV" → 4 thẻ số liệu (Doanh thu `info`, Tiền vào `ok`, Hoàn tiền `bad`, Vé · đơn `special`) → trục giờ Hôm nay → hai cột: thẻ Dòng tiền | thẻ Cần để ý + Ca quầy đang mở → thẻ Đơn mới hôm nay.
- **Đơn hàng và các trang danh sách** (Phim, Bảng giá, Combo, Voucher, Bài viết, Banner, Bình luận, Người dùng): đầu trang → (thẻ số liệu nếu có) → thẻ danh sách. Phim: chip tuổi cạnh tên, chip `Đang chiếu`/`Sắp chiếu`/`Đã kết thúc` và chip `Đang hiển thị`/`Đang ẩn` tách nghĩa. Voucher: mức giảm có đơn vị (`100%`, `20.000 đ`).
- **Soát vé (Staff):** giữ bố cục ba cột đã triển khai. Kết quả soát là một thẻ lớn tô nền theo nghĩa (Hợp lệ `ok`, Vé đã dùng `warn`, Sai suất/Không thuộc rạp `bad`) với chữ trạng thái 28/700, ghế, suất, mã đơn; nút "Cho vào" cam. Bảng vé dùng chip Đã vào `ok` / Chưa vào `off`. Dải số Hiệu lực/Đã vào/Chưa vào/Tỉ lệ là thẻ số liệu nhỏ.
- **Lịch chiếu (Admin, chốt 14/11/2026, phác thảo trên canvas "CinePrime Admin/Staff — phác thảo", trang "Lịch chiếu — 3 tab + ngăn Thêm suất (14/11)"):** ba màn **Theo ngày** (`/admin/schedule`) · **Danh sách** (`/admin/manage/showtimes`) · **Xếp lịch tuần** (`/admin/showtimes/new`). **Cập nhật 14/11/2026 (người dùng yêu cầu bỏ dải tab, "không phải scrolling"):** Lịch chiếu là **nhóm trên sidebar** (`details.nav-group`, mũi tên xoay 90° khi mở): bấm "Lịch chiếu" mở ra ba mục con thụt vào, kẻ trái `--line`, cao 32px, mục đang mở nền `--brand-soft` chữ `--brand` 600; nhóm tự mở khi đang ở một trong ba màn và các mục con mang theo ngữ cảnh. Ba trang **không có breadcrumb, tiêu đề hay dải tab**; trang bắt đầu ngay bằng **một thanh ngữ cảnh** một hàng (thẻ trắng, ô chọn không nhãn có `aria-label`: Rạp · ← ngày/tuần → · Hôm nay · Phim · Phòng · Trạng thái [· Tìm] … nút cam "+ Thêm suất chiếu" có `kbd` N ở cuối), rồi một thẻ chiếm phần còn lại của màn hình; phần dài (trục giờ, bảng) cuộn bên trong thẻ (`data-fit`), trang không cuộn. Lọc tự áp dụng khi đổi ô chọn. Phím tắt chỉ còn trong `title` và `kbd` trên nút: `N` thêm suất, `←`/`→` đổi ngày (tuần), `T` hôm nay (tuần này), `Esc` đóng ngăn. Các khu khác (Rạp & phòng, Kinh doanh, Nội dung, Hệ thống) vẫn dùng dải tab cho đến khi người dùng muốn đổi. Bỏ hộp "Rạp đang quản lý", liên kết chéo, ô tìm "Phim / Phòng" ở Theo ngày. CSS: `static/css/showtime-hub.css` (Theo ngày, Danh sách), `showtime-batch.css` (Xếp lịch tuần), `showtime-editor.css` (hộp Sửa suất) — chỉ dùng token, không hex rời ngoài khai báo `--film-0…7`.
  - **Theo ngày:** một thẻ: đầu thẻ là **dải số liệu một dòng** (ô nhỏ viền trái 3px theo nghĩa: số suất · phòng `info`, mở bán · đang chiếu `ok`, nháp kèm "mở bán →" `off`, phòng trống `warn`) + chú giải ở bên phải → "Trục giờ theo phòng": mỗi phòng một hàng 48px (tên, định dạng · số ghế · số suất), thước giờ dính trên đầu, nhãn giờ hiện tại là ô cam trên thước, thước giờ từ 08:00 (giãn theo suất), mỗi suất là thanh cao 36px `info` nhạt (Nháp `off` viền đứt, đã qua mờ, đang chiếu viền `--brand` 2px), vạch đậm 4px đáy thanh = tỉ lệ ghế đã bán, đuôi `--line-soft` = 15 phút dọn phòng, vạch cam "bây giờ" + nhãn giờ `--accent-ink`. Bấm thanh → hộp Sửa suất; bấm khoảng trống → Xếp lịch tuần mở ngăn Thêm suất đúng phòng, ngày, giờ (làm tròn 15 phút); khoảng trống ≥ 3 giờ hiện nút "+ Thêm suất từ HH:MM" khi rê chuột. Chân thẻ (thay thẻ "Cần để ý" riêng): khoảng trống dài, suất nháp, phòng chưa có suất xếp trên một dòng; không có gì thì chip `ok` + dòng hướng dẫn.
  - **Danh sách:** 20 dòng/trang (dòng 36px, bảng cuộn trong thẻ, đầu bảng dính), xếp theo phòng rồi giờ, dòng nhóm phòng nền `--card-head` (định dạng · số ghế · số suất trong ngày). Cột: ô chọn · Giờ chiếu · Phim · Định dạng · Ngôn ngữ · Đã bán (thanh 64px + "x / y") · Phụ thu (căn phải) · Trạng thái (chip) · Sửa. Chọn dòng → dải `--brand-soft` trên bảng: "Đã chọn N suất · a nháp · b đang mở bán" + "Mở bán a suất nháp" (xanh) + "Huỷ N suất" (viền đỏ) + "Bỏ chọn"; tất cả hoặc không (API `POST /api/admin/showtime-batches/showtimes/bulk`). Suất có vé/giữ ghế, đã bắt đầu hoặc đã huỷ không chọn được (ô mờ có lời giải thích).
  - **Xếp lịch tuần:** thanh ngữ cảnh riêng (Rạp · tuần · Cả tuần/Theo giờ · + Thêm suất `kbd` N · Chép ngày · Làm mới · Excel); hai chế độ cùng công cụ; không còn dải hướng dẫn 3 bước. Bảng tuần và ngăn Thêm suất co theo chiều cao cửa sổ (ngăn kết thúc ở đáy cửa sổ, thanh lưu ẩn khi ngăn mở). **Giờ trong ngăn là ô chữ 24 giờ `HH:MM`** (không dùng `input type=time`: trình duyệt tiếng Việt hiện 12 giờ "02:15 CH", ô hẹp cắt mất "CH" nên đọc thành 02:15); nhận "9", "930", "9h30", "21.15", Enter để áp dụng. Ô suất: nền theo trạng thái (Mở bán `info`, Nháp `off`, Mới chưa lưu nền trắng viền đứt `--accent-ink`, lỗi `bad`, đang xem trước viền đứt `--faint`), vạch trái 3px = màu phim (`--film-0…7`, chỉ để nhận ra phim, không mang trạng thái). **Ngăn "Thêm suất chiếu"** (cột phải 384px, cạnh lịch, không che lịch; thay hộp thoại thêm suất + ngăn "Nhiều suất nối tiếp"): Phòng (chip) → Phim (danh sách có thứ tự, "+ Thêm phim"; từ 2 phim hiện "Xen kẽ / Lần lượt", Lần lượt có bộ đếm số suất mỗi phim) → Ngày (7 chip, ngày đã qua tắt, ngày ngoài thời gian chiếu viền đứt `warn`; chọn nhanh Cả tuần · T2–T6 · Cuối tuần) → Giờ ("Một suất / Nhiều suất nối tiếp", Suất đầu từ · Suất cuối bắt đầu trước · "N suất/ngày") → mỗi ngày một dải giờ 08:00–02:00 (xám = suất đã có, số = suất sắp thêm, kéo để dời và ghim) và các ô giờ sửa được (sửa = ghim, "Bỏ ghim"). Đổi phòng/phim khi có giờ ghim thì hỏi trước; đổi giờ suất cuối không bỏ ghim. Thanh đáy: "N suất mới chưa lưu · x cần sửa · y chưa đủ điều kiện mở bán" + **Lưu nháp** (viền, lưu thẳng khi không có lỗi/cảnh báo) + **Lưu và mở bán N suất** (cam, luôn có bước xác nhận). Bỏ ô "Sau khi tạo".
  - **Hộp Sửa suất:** giữ bố cục; chip trạng thái theo bảng nghĩa (Đang bán vé `info`, Nháp `off`, Có vé / giữ ghế `warn`), dải suất của phòng trong ngày dùng token. Server từ chối dời hoặc mở bán suất về giờ đã qua.
- **Bán vé tại quầy (Staff):** dải ca nền `counter` nhạt (ca, đơn, vé, tiền mặt trong két, "Đơn trong ca", "Đóng ca"); ba cột 1 · Suất → 2 · Ghế → 3 · Đơn; nút chính cam "Giữ N ghế · 5 phút" chỉ hiện khi đã chọn ghế.

- **Thành viên (chốt 17/11/2026, đã triển khai mã):** bốn tab Khách hàng · Hạng & quyền lợi · Ưu đãi & quà · Suất hội viên; tab có huy hiệu số (khách, quà chờ trao, suất hội viên sắp tới). Mọi màn có **dải bậc hạng** `.tier-strip`: 5 bậc nối bằng mũi tên mảnh (chip hạng, số khách, mốc chi tiêu; bấm sang danh sách khách hoặc quy tắc hạng) + ba ô "Quà chờ trao", "Voucher trong ví", "Suất hội viên sắp tới" (ô quà nền `warn` khi có quà chờ). Màu hạng chỉ ở chip và dải, token `--tier-silver/gold/platinum/diamond/vip`. **Hạng & quyền lợi** = danh sách hạng bên trái + một ngăn sửa (ba khối: Điều kiện & giá hội viên · Quà tự động · Khách thấy trong tài khoản), một thanh lưu cố định cho cả 5 hạng; ô chọn voucher chỉ ghi mã, mô tả nằm dưới. **Chi tiết khách** = thẻ hạng bên trái (số liệu + thao tác gập mở) + tab Ví voucher · Quà · Sổ điểm · Đơn · Lịch sử hạng. **Ưu đãi & quà**: chiến dịch ba bước kèm ước tính số khách nhận, danh sách mẫu voucher có liên kết Sửa, quà tại quầy theo trạng thái, kho lưu niệm. **Suất hội viên**: ngày + giờ 24 giờ HH:MM (không `datetime-local`), chip "Hội viên X+" chỉ hiện ở suất có giới hạn. Phần tử khối không dùng thẻ `nav` (CSS chung tô nền tối thẻ `nav` trong vùng nội dung) — dùng `div role="navigation"`.
**Không làm:** gradient, emoji, bóng đổ trên thẻ thường; màu không có nghĩa (thể loại nhiều màu, icon tô màu ngẫu nhiên); pill tròn cho trạng thái; chỉ dùng màu không có chữ; nhiều hơn một nút cam mỗi màn; nút đen nặng cho "Thêm mới"; dải "—" lặp lại cho số 0; ký hiệu "₫"; nhóm sidebar rỗng; thay đổi cụm logo + nút sidebar.

### 3.5. Phong cách "Studio Cinema Pearl" — vát hai góc đối (chốt 15/11/2026)

**Phạm vi:** áp dụng cho cả ba vai trò (khách, Admin, Staff). Mục này **thay** màu, phông và hình khối ở 3.0–3.4; **bố cục, luồng thao tác và bảng nghĩa màu trạng thái ở 3.0 và 3.4 giữ nguyên**. Nguồn: bộ Cinema-Pearl-Agent-Kit do người dùng cung cấp (chỉ lấy phong cách, không lấy bố cục sidebar trái của trang khách, không lấy các nhóm đóng/mở của sidebar Admin). Phác thảo: canvas "CinePrime Admin/Staff — phác thảo", trang "Cinema Pearl — đổi phong cách (15/11)". Trạng thái triển khai mã: xem README.

Token nằm ở một nơi duy nhất: `static/css/cineprime-theme.css`, nạp **trước** mọi stylesheet khác ở cả ba layout. Các file CSS theo vai trò không khai báo lại token màu.

| Token | Giá trị | Vai trò |
|---|---|---|
| `--bg` | `#DFE4EC` | Nền trang |
| `--bg-elev` | `#EAEFF5` | Header, sidebar, footer, dải nền nâng |
| `--surface` | `#F4F6F9` | Thẻ, ô nhập, bảng (không dùng trắng `#FFFFFF` cho bề mặt) |
| `--surface-2` / `--card-head` | `#E4E9F1` / `#EBEEF4` | Khối phụ, hàng rê chuột / đầu bảng, đầu thẻ |
| `--line-soft` / `--line` / `--line-strong` | `#DBE1EA` / `#CBD3DF` / `#AEBACB` | Kẻ trong bảng / viền thẻ / viền ô nhập, nút |
| `--ink` / `--ink-2` / `--muted` | `#151B26` / `#334155` / `#586477` | Chữ chính / chữ phụ / chú thích |
| `--faint` | `#7A8699` | Chỉ cho chi tiết không cần đọc (2,9:1 trên nền, không dùng cho chữ nội dung) |
| `--accent` / `--accent-hover` | `#C83728` / `#AD2D20` | Hành động chính, mục đang chọn, ghế đang chọn |
| `--accent-ink` / `--accent-on` | `#A8281B` / `#F8FAFC` | Chữ đỏ trên nền sáng, liên kết / chữ trên nền đỏ |
| `--accent-soft` / `--accent-line` | đỏ 10% / đỏ 34% | Nền và viền của mục đang chọn |
| `--btn-dark` / `--btn-dark-hover` | `#1E2532` / `#2B3445` | Nút phụ có trọng lượng (Đăng ký, Lọc, Xác nhận) |
| `--focus-ring` | `#2563EB` | Viền focus bàn phím; cobalt không còn là màu chủ đạo |

- **Màu trạng thái** (ok / info / warn / bad / off / counter / special / food) giữ nghĩa như bảng ở 3.4, chỉ đổi sắc độ cho hợp nền: xem khối "Semantic Tokens" trong `cineprime-theme.css`. Info chuyển từ xanh dương sang slate.
- **Tên token cũ** (`--brand`, `--blue`, `--orange`, `--navy`, `--canvas`, `--card`, `--paper`, `--soft`...) còn tồn tại dưới dạng alias trong theme để mã cũ chạy được: `--brand`/`--blue` trỏ về đỏ đậm `--accent-ink`, `--orange` trỏ về `--accent`. Mã mới dùng tên theo nghĩa, không dùng alias.
- **Phông** (tự host trong `static/fonts`, OFL, đủ bộ Latin / Latin mở rộng / tiếng Việt): tiêu đề, tên phim, số liệu dùng **Be Vietnam Pro** 500/600/700; nội dung, biểu mẫu, bảng dùng **Plus Jakarta Sans** 400/500/600/700; mã vé, mã đơn, giờ trong lưới lịch dùng **JetBrains Mono** 500/600. Không gọi phông từ mạng ngoài (CSP `'self'`).
- **Cỡ chữ:** nội dung 15px/1,6 (bảng vận hành 14–14,5px); thang 12 / 13,5 / 15 / 16 / 18 / 22 / 28px. Các cỡ tiêu đề theo màn ở 3.0 giữ nguyên.
- **Hình khối:** không bo tròn (`border-radius: 0`, trừ hình tròn thật như chấm, vòng quay chờ). Dấu hiệu nhận diện là **vát 45° ở góc trên trái và dưới phải**, hai góc còn lại vuông; không vát bốn góc. Bốn cỡ: `--cut-xs` 5px (chip, ghế, nhãn tuổi), `--cut-sm` 7px (nút, ô nhập, tab), `--cut-md` 10px (thẻ), `--cut-lg` 14px (hero, khung lớn). Viền 1px; mục đang chọn có viền đỏ (1px + vòng trong 1px) và nền `--accent-soft`.
- **Cách vát:** `clip-path` + hai dải `linear-gradient` cứng (135° và -45°) vẽ cạnh chéo. Đây là hình học, **không** phải gradient trang trí; quy tắc "không gradient" ở 3.0/3.4 vẫn áp dụng cho mọi gradient chuyển màu. Đổi nền phần tử vát bằng `background-color`, không dùng `background` viết gộp (sẽ xoá cạnh chéo). Khối chứa menu thả xuống, hộp thoại hoặc phần tử tràn ra ngoài **không** được `clip-path` trực tiếp: dùng `.cp-bevel-frame` (vẽ khung bằng lớp giả) hoặc để vuông.
- **Đường chéo = viền** (chốt 17/11/2026): cạnh chéo là một phần của đường viền, **cùng màu và cùng độ dày** với hai cạnh kề, nên viền chạy liền quanh cả hai góc vát; không mũ góc đậm hơn hay dày hơn (admin đã bỏ mũ 3–4px cũ vì nhìn "gaming"; trang khách vẫn giữ mũ đỏ 4px ở tab đang chọn và mũ tối 3px ở nút chính, chờ người dùng quyết). Quy tắc mã: `--bevel-corner` luôn trỏ cùng token với `border-color` của trạng thái đó (hover, focus, chip theo tông). Phần tử không viền (ô icon, nút nền đặc) chỉ cắt góc. Ô nhập bị trình duyệt tự điền mất `background-image`, nên vẽ viền bằng `border-image` SVG (`input:autofill`).
- **Nút:** đỏ `--accent` chữ `--accent-on` cho hành động chính, **tối đa một nút mỗi khối** (thay nút cam chữ đen); slate `--btn-dark` cho hành động phụ có trọng lượng (thay nút xanh dương); viền cho phần còn lại. Cao 44px ở trang khách, 34–40px ở Admin/Staff.
- **Chip trạng thái:** vát 5px, nền nhạt + chữ đậm cùng tông + viền 1px (thay ô bo 4px ở 3.4). Vẫn luôn có chữ, không chỉ màu.
- **Logo:** biểu tượng `cineprime-mark.svg` + "Cine" màu `--ink` + "Prime" màu `--accent` (thay xanh `#1459A5` + cam). Bố cục cụm đầu trái (nút ẩn/hiện sidebar, mũi tên lùi/tiến, logo) giữ nguyên.
- **Focus:** viền focus nằm ngoài bị `clip-path` cắt, nên phần tử vát dùng vòng trong (`box-shadow: inset 0 0 0 1px var(--focus-ring)`) và đổi màu viền sang `--focus-ring`.
- **Staff:** giữ nguyên luồng soát vé bằng camera quét QR và nhập tay; chỉ đổi màu, phông, hình khối.

#### Lịch chiếu — màu theo phim (chốt 15/11/2026)

Người dùng chọn lấy **lớp hình ảnh** của bảng tuần trong kit Cinema Pearl và giữ nguyên chức năng đã chốt 14/11 (ba mục sidebar, thanh ngữ cảnh, ngăn Thêm suất, hai nút lưu, phím tắt, ô giờ HH:MM). Áp dụng cho cả ba màn Theo ngày, Danh sách, Xếp lịch tuần.

- **Nền của suất = phim.** Tám token `--film-0…7` trong `cineprime-theme.css` (đổi 16/11/2026 vì bảng cũ quá giống nhau): `#2563EB` lam · `#EA580C` cam · `#059669` lục · `#DB2777` hồng · `#CA8A04` vàng · `#7C3AED` tím · `#0891B2` lam ngọc · `#65A30D` xanh nõn — mã liền nhau cách xa nhau trên vòng màu. Một phim luôn dùng token `mã phim % 8` ở cả ba màn (lớp `.film-N` hoặc biến `--tone`). Hai phim có mã lệch nhau bội số của 8 sẽ trùng màu; khi đó đọc theo tên.
- Từ `--tone` suy ra ba màu: nền `--tone-bg` (22% màu phim trên `--surface`), viền `--tone-line` (55%), vạch trái và góc vát `--tone-bar` (88% màu phim trộn `--ink`). Không viết mã màu phim ở nơi khác.
- **Trạng thái đọc ở viền, không ở nền:**

| Trạng thái | Dấu hiệu |
|---|---|
| Mở bán | Viền liền màu phim |
| Nháp | Viền đứt `--muted` |
| Mới, chưa lưu | Viền `--accent` hai lớp |
| Đang xem trước | Viền đứt `--line-strong`, nền `--surface` |
| Đã chiếu | Nền `--surface-2`, chữ `--muted`, vạch trái `--line-strong` |
| Trùng lịch | Nền `--bad-bg`, viền `--bad-bar`, có dòng lý do |
| Đang chiếu (Theo ngày) | Viền 2px `--ink` |

- Giờ của suất viết bằng `--font-mono`. Vạch 3px dưới thanh suất (Theo ngày) và thanh "Đã bán" (Danh sách) dùng `--tone-bar`.
- **Xếp lịch tuần** (cập nhật 16/11/2026): giữ cột "Phim để bấm thêm" bên trái, nhưng danh sách phim **thu vào một combobox** (nút hiện phim đang chọn, nền màu phim). Bấm thì danh sách xổ ra thành lớp nổi ngay dưới nút, đúng bề rộng cột phim và dừng trên thanh lưu (không đẩy, không đè lên lịch); bấm một phim để chọn và làm nổi các suất của nó (Cả tuần), "Hiện lại mọi phim" để bỏ làm nổi. **Kéo một phim từ danh sách thả vào lịch**: vào một ngày (Cả tuần) thì mở ngăn Thêm suất với phim, phòng, ngày đó; vào trục giờ (Theo giờ) thì thêm một suất tại chỗ thả, khớp bước 5 phút. Cột ẩn khi ngăn Thêm suất mở (Cả tuần). Khi ngăn mở, tab phòng trên lịch ẩn đi; **phòng chọn bằng một ô chọn** trong ngăn. Mỗi dòng phim trong ngăn là một ô chọn (đổi phim tại dòng), "+ Thêm phim khác" thêm dòng. Nút **Cả tuần | Theo giờ** là một khối phân đoạn liền: rãnh `--surface-2`, đoạn đang chọn nền trắng có gạch dưới `--accent`.
- **Theo ngày:** có dải "Phim trong ngày" làm chú giải màu; bấm một phim để lọc theo phim đó. Giờ hiện tại (16/11/2026) là **mũi chỉ ▲ dưới hàng phòng cuối** kèm nhãn `HH:MM` viền `--accent`, dính đáy khung cuộn và tự chạy theo đồng hồ (mỗi 15 giây); không còn đường kẻ đỏ xuyên qua các suất hay nhãn trên thước giờ. Thanh ngữ cảnh dùng cùng lưới với Danh sách.
- **Danh sách:** tên phim nằm trong nhãn màu phim. Thanh ngữ cảnh (16/11/2026) là lưới: cửa sổ ≥ 1600px một hàng; hẹp hơn thì hàng 1 là rạp · ngày · "+ Thêm suất" (phải), hàng 2 là Phim · Phòng · Trạng thái · ô tìm trải đều. Không có nút "Lọc" hiện ra (ô chọn tự lọc, Enter trong ô tìm gửi form). Không phân trang: cả ngày của một rạp hiện trong một khung bảng cuộn bên trong (tiêu đề cột dính trên), trang không cuộn; dưới khung chỉ ghi "N suất trong ngày".
- **Hộp xác nhận lưu, "Xem từng suất":** cột giờ chỉ ghi HH:MM phông đơn cách (ngày nằm dưới giờ bắt đầu, "hôm sau" khi qua ngày), cột Phim/phòng rộng 38%, "Hợp lệ" và nút "Xem / sửa" xếp dọc.

### 3.6. "Pearl × Vé" — phong cách chung cho Khách, Admin, Staff (chốt 17/11/2026)

Người dùng thấy giao diện "hơi gaming và AI", so ba hướng A/B/C trên canvas rồi chọn **gộp Cinema Pearl (3.5) với hướng C "Vé xem phim"** cho cả ba vai. Phác thảo: canvas "CinePrime Admin/Staff — phác thảo", trang "Pearl × Vé".

- **Giữ từ Pearl:** nền `--bg` #DFE4EC, thẻ `--surface`, đỏ `--accent`, Be Vietnam Pro, vát góc trên-trái + dưới-phải ở thẻ, nút, ô nhập; đường chéo = viền (3.5).
- **Mục đang chọn** (sidebar, tab khách, ngày chọn): nền `--accent-tint` + chữ `--accent-ink`, **không** khung đỏ dày, không mũ góc đậm. Thay quyết định "nền `--brand-soft`" cũ.
- **Tab kiểu thẻ vé** (`.hub-tabs`, `.tabs`): chỉ vát góc trên-trái, nền `--surface-2`; tab đang mở nền `--surface`, vạch đỏ 2px ở cạnh trên.
- **Cuống vé cho số liệu** (`.kpi-card`, `.stat-card`): dòng nhãn tách phần số bằng **đường xé nét đứt có hai khuyết tròn** (khuyết cùng màu nền trang); bỏ vạch màu 3px trên đầu thẻ.
- **Mã vé / mã đơn / mã voucher** (`.ticket-code`): phông đơn cách, ô viền nét đứt.
- **Tem trạng thái**: chip vát 5px, chữ đậm 700, viền cùng tông (giữ bảng ngữ nghĩa 3.4).
- **Vé điện tử, kết quả soát vé, đơn trong "Vé của tôi"**: thẻ vát, đường xé tách phần thông tin với mã.
- **Không** dùng hình vé cho bảng, form, sidebar, hộp thoại: giữ phẳng. Tiêu đề bảng có viền dưới nét đứt.
- Vẫn: không gradient chuyển màu, không bóng trên thẻ, không emoji; tối đa một nút đỏ mỗi màn vận hành.
- **Màn rộng (17/11/2026):** ở scale 100% (cửa sổ ~1920px) không chặn chiều rộng làm hở hai bên. Admin: khung và form lấp đầy (bỏ 1800px / 1100px). Khách: `--container:clamp(1328px,83.4vw,1680px)` — ≤1536px giữ như cũ, 1920px ≈ 1600px.

## 4. Component Specifications

### Header

- Topbar thông báo, thông tin hỗ trợ (có thể ẩn mobile).
- Header logo + ô tìm kiếm + vé của tôi + đăng nhập/đăng ký.
- Navigation tối: Mua vé, Phim, Lịch chiếu, Khuyến mãi, Góc điện ảnh, Rạp/Giá vé.
- Mobile menu bật/tắt được, không tràn ngang.

### Hero Carousel

- 3 banner phim, overlay tối để bảo đảm chữ trắng tương phản tốt.
- Hiển thị: nhãn phim, tựa phim lớn, mô tả ngắn, nút Đặt vé và Xem chi tiết.
- Có điều hướng trước/sau và indicator.
- Hình banner phải là ảnh được sở hữu, mua bản quyền hoặc được phép sử dụng.
- **Chồng poster (chốt 17/11/2026):** từ 1091px, bên phải banner phim là chồng poster: phim hiện tại ở trước, các phim kế tiếp nhỏ dần (−13%) và tối dần phía sau; mỗi lần chuyển, poster sau trượt lên trước, poster cũ mờ sang trái; nội dung banner mờ dần và trượt lên (0,7 s). Banner khuyến mãi ẩn chồng poster. Ảnh ngang giãn hết chỗ còn lại. Mặc định tự chuyển **5 giây** (Admin chỉnh 5–10). ≤1090px ẩn chồng poster.
- **Ưu đãi / Góc điện ảnh (chốt lại 17/11/2026):** thẻ có ảnh = **poster dọc 2:3** trên, **khung viền nội dung** dưới (loại · tiêu đề ≤2 dòng · mô tả ngắn ≤3 dòng · ngày); ảnh giữ góc vát trên-trái, khung giữ góc vát dưới-phải nên cả thẻ là một khối vát. Trang chủ (cập nhật 17/11/2026): Ưu đãi và Góc điện ảnh **nằm cạnh nhau** trên một hàng, mỗi bên 3 thẻ poster (≤1090px xếp chồng; ≤640px vuốt ngang). Bài chưa có ảnh dùng **bìa chữ** cùng khổ 2:3 (Ưu đãi nền đỏ thương hiệu, Bài viết nền navy) nên một hàng không lẫn hai kiểu thẻ. Admin → Nội dung có tab Tất cả · Ưu đãi cho bạn · Góc điện ảnh · Trang thông tin, cột "Hiện ở", mô tả ngắn và ảnh poster nhỏ ở từng dòng. Admin chỉ nhận ảnh dọc (từ 4:5) cho Ưu đãi / Bài viết, ảnh ngang dùng cho Banner. Admin nhập "Mô tả ngắn dưới ảnh" (`contents.summary`); để trống thì lấy đoạn đầu nội dung. Trang chi tiết hiện ảnh dạng poster rộng tối đa 320px.
- **Bố cục mới (cập nhật 17/11/2026, thay số cột ở dòng trên):** Ưu đãi và Góc điện ảnh **nằm cạnh nhau** trên một hàng, mỗi bên 3 thẻ poster (≤1090px xếp chồng; ≤640px vuốt ngang). Bài chưa có ảnh dùng **bìa chữ** cùng khổ 2:3 (Ưu đãi nền đỏ thương hiệu, Bài viết nền navy) nên một hàng không lẫn hai kiểu thẻ. Admin → Nội dung có tab Tất cả · Ưu đãi cho bạn · Góc điện ảnh · Trang thông tin, cột "Hiện ở", mô tả ngắn và ảnh poster nhỏ ở từng dòng.

### Mua vé nhanh

- Select bắt buộc: phim → rạp → ngày → suất.
- CTA cam "MUA VÉ".
- Với backend thật, các select nên phụ thuộc nhau: đổi phim có thể thay tập rạp/suất; đổi ngày phải tải lại suất.
- Lỗi validation: tập trung vào trường thiếu; không điều hướng nếu chưa đủ dữ liệu.

### Movie Card

- Poster tỷ lệ 2:3, nhãn tuổi P/T13/T16/T18, IMAX (nếu có), điểm đánh giá (nếu có thật).
- Hover overlay desktop và nút chạm rõ ràng trên mobile.
- Thông tin sau poster: tên phim, thể loại, thời lượng.
- Với phim sắp chiếu thay CTA mua vé bằng "Xem chi tiết" / "Nhắc tôi" tùy nghiệp vụ.

### Lịch chiếu

- Dải 7 ngày; ngày chọn active xanh.
- Card phim với các giờ dạng nút, chọn giờ sang chọn ghế.
- Với bản thật: lọc theo khu vực, rạp, định dạng, trạng thái còn vé; ẩn giờ đã qua.

### Seat Map

- Phòng chiếu nhìn từ trên xuống, màn hình ở đầu sơ đồ.
- Màu ghế: thường `#E3E9EF`, VIP `#D4E8FA`, đã bán `#C8CDD3`, đang chọn `#F47B20`.
- Có chú giải màu và tên ghế; giới hạn mua ví dụ 8 ghế/giao dịch (quy định thực tế theo nghiệp vụ).
- Chỗ ngồi đã bán không thể chọn; giao diện demo giả lập ghế đã bán cố định.
- Tổng tiền cập nhật theo loại ghế; giá demo: thường **85.000đ**, VIP **105.000đ**.
- Backend thật cần giữ ghế tạm thời (TTL), kiểm tra xung đột cạnh tranh và tự giải phóng nếu chưa thanh toán.

### Modal, Toast và trạng thái

- Modal đóng được bằng nút X, click overlay, phím Escape.
- Toast ngắn cho lỗi thao tác / điều kiện chưa đủ.
- Bản triển khai thêm: skeleton loading, retry, empty states, offline/timeout, session expired, ghế vừa bị người khác mua.

## 5. User Flows

### 5.1. Khách hàng

```text
Trang chủ
   ↓
Chọn phim → Xem chi tiết
   ↓
Chọn rạp → Chọn ngày → Chọn suất chiếu
   ↓
Chọn ghế (xem sơ đồ + tổng tiền)
   ↓
[Chọn combo nếu có]
   ↓
Xác nhận đơn hàng → Thanh toán
   ↓
Webhook thanh toán xác thực thành công
   ↓
Tạo vé điện tử / QR → Lịch sử vé
```

### 5.2. Quản trị viên

```text
Đăng nhập Admin → Dashboard
   ├─ CRUD phim / diễn viên / thể loại
   ├─ CRUD rạp / phòng / sơ đồ ghế
   ├─ Quản lý suất chiếu / giá vé
   ├─ Quản lý khuyến mãi / banner
   ├─ Xem đơn hàng / hoàn vé (theo quy định)
   └─ Báo cáo doanh thu / công suất phòng
```

### 5.3. Nhân viên

```text
Đăng nhập Staff → Danh sách suất hôm nay
   ├─ Quét QR vé
   ├─ Xác minh trạng thái vé và suất
   └─ Check-in một lần / thông báo vé không hợp lệ
```

## 6. Dữ liệu và trạng thái UI cần backend cung cấp

| Resource | Trường dữ liệu tối thiểu |
|---|---|
| `Movie` | id, slug, title, description, posterUrl, bannerUrl, duration, ageRating, releaseDate, status |
| `Cinema` | id, name, address, city, status |
| `Room` | id, cinemaId, name, layoutConfig, screenType |
| `Seat` | id, roomId, row, number, type, active |
| `Showtime` | id, movieId, roomId, startsAt, endsAt, language, format, status |
| `SeatPrice` | showtimeId, seatType, amount, currency |
| `SeatHold` | showtimeId, seatId, user/session, expiresAt, status |
| `Order` | id, userId, showtimeId, total, status, expiresAt |
| `OrderItem/Ticket` | id, orderId, seatId, price, qrToken, checkInStatus |
| `Payment` | id, orderId, provider, transactionRef, status, paidAt |
| `Promotion` | id, title, rules, validFrom, validTo, quota, code |
| `Article` | id, title, excerpt, coverUrl, publishedAt |

**Bảo mật / giao dịch:** không tin tổng tiền từ frontend; backend phải tính lại theo giá vé; khóa ghế bằng transaction/unique constraint; xác thực webhook và idempotency; kiểm tra quyền role trước mọi API admin/staff; QR vé nên là token khó đoán có hạn sử dụng/định danh.

## 7. Hướng triển khai Spring Boot

### Cấu trúc gợi ý

```text
src/main/resources/
  templates/
    fragments/header.html
    fragments/footer.html
    home/index.html
    movie/list.html
    movie/detail.html
    booking/select-showtime.html
    booking/select-seats.html
    booking/checkout.html
    booking/success.html
    auth/login.html
    account/tickets.html
    admin/dashboard.html
    staff/checkin.html
  static/
    css/styles.css
    js/app.js
    images/...
```

Với **Spring Boot + Thymeleaf**, nên tách `index.html` thành fragment, đưa CSS/JS vào `static`, sau đó thay dữ liệu giả bằng controller + DTO. Nếu chọn REST API + frontend tách biệt, có thể tái sử dụng CSS và thiết kế, chuyển các view thành React/Vue component.

### Endpoint gợi ý

- `GET /api/movies?status=NOW_SHOWING`
- `GET /api/movies/{id}`
- `GET /api/cinemas?city=...`
- `GET /api/showtimes?movieId=...&cinemaId=...&date=...`
- `GET /api/showtimes/{id}/seats`
- `POST /api/bookings/holds` (giữ ghế, backend kiểm tra xung đột)
- `POST /api/bookings` (chốt đơn)
- `POST /api/payments/create`
- `POST /api/payments/webhook` (xác thực chữ ký)
- `GET /api/me/tickets`
- `POST /api/staff/tickets/check-in`

## 8. Checklist hoàn thiện trước khi bảo vệ đồ án

- [ ] Mọi nút và màn hình dùng được trên desktop, mobile.
- [ ] Phân quyền 3 nhóm: Customer, Admin, Staff.
- [ ] CRUD phim, rạp, phòng, ghế, suất chiếu; có validation và soft delete phù hợp.
- [ ] Không trùng suất trong cùng phòng và không đặt trùng ghế cùng suất.
- [ ] Có giữ ghế tạm và giới hạn thời gian thanh toán.
- [ ] Có lịch sử đặt vé, trạng thái đơn và QR check-in.
- [ ] Không tin giá tiền do client gửi lên.
- [ ] Có test: chức năng đặt vé, xung đột ghế, huỷ thanh toán, quyền người dùng.
- [ ] Có data demo đẹp, screenshot, tài khoản demo phân quyền.
- [ ] Có deploy thật, README kiến trúc và hướng dẫn sử dụng.

## 9. Các file kèm theo

- `index.html`: giao diện chính.
- `styles.css`: toàn bộ style và breakpoint.
- `script.js`: tương tác frontend.
- `assets/*.svg`: bộ ảnh poster/banner minh họa tự tạo.
- `UI-Template.html`: bản độc lập (HTML chứa CSS/JS/assets), mở trực tiếp không cần giải nén.
- `README.md`: hướng dẫn và giới hạn demo.

**Lưu ý bản quyền:** tham khảo cấu trúc và phương pháp thiết kế từ Galaxy Cinema, không sử dụng thiết kế/template này để giả mạo thương hiệu Galaxy Cinema. Đổi nhận diện và dùng nội dung có quyền sử dụng trước khi phát hành thương mại.
