---
name: cineuxui
description: Skill UX/UI duy nhất của CinePrime. Dùng khi re-design, cải thiện, phác thảo, dựng hoặc soát giao diện bất kỳ màn nào (trang khách, Admin, Staff) — kể cả khi người dùng chỉ nói "làm đẹp lại", "trang này nhàm", "thêm màu", "soát UI", "màn này ổn chưa", hoặc gửi ảnh chụp màn hình. Gọi bằng /cineuxui <màn hoặc vấn đề>.
argument-hint: "<màn hoặc vấn đề, ví dụ: admin phim | staff soát vé | trang chi tiết phim> [phác thảo | sửa mã | soát]"
---

# /cineuxui — UX/UI CinePrime

Đối số: `$ARGUMENTS`. Trống thì hỏi đúng một câu: màn nào, và lần này **phác thảo**, **sửa mã** hay **soát**.

## 0. Nguồn chuẩn và phạm vi

- Token và thành phần nằm ở **UI-Template.md** (CLAUDE.md quy định đây là nguồn chuẩn duy nhất về design token). Đọc mục tương ứng trước mọi việc:
  - Trang khách: §3.0 + §4, responsive desktop **và** mobile (375×812).
  - Admin/Staff: §3.4 "Thẻ màu nhạt" (phương án B, chốt 13/11/2026), chỉ desktop ≥ 1366×768.
- Skill này giữ quy trình, quyết định đã chốt, quy tắc chữ và bảng soát. Khi quyết định mới được chốt: ghi token/thành phần vào UI-Template.md, ghi quy trình/quyết định vào skill này. Không tạo skill hay tài liệu UI khác.

## 1. Quyết định đã chốt với người dùng (không tự đổi)

1. **Giữ nguyên** cụm đầu trái thanh trên cùng: nút ẩn/hiện sidebar, mũi tên lùi/tiến, logo `cineprime-mark.svg` + "Cine" xanh #1459A5 "Prime" cam. Người dùng rất hài lòng với cụm này.
2. **Có màu, nhưng màu có nghĩa** (13/11/2026). Bản đen trắng "Sổ suất chiếu" (12/11) bị đánh giá thấp hơn bản gốc vì nhàm, khó quét mắt — không quay lại hướng đó.
3. **Trạng thái là chip vuông nhạt:** ô bo 4px, nền nhạt + chữ đậm cùng tông + viền 1px ("Đang hoạt động" xanh lá nhạt). Không ô 8px trơn, không pill tròn.
4. **Phương án B:** nền trang `#F4F6F9`, khối là thẻ trắng viền 1px bo 10px; thẻ số liệu có vạch màu 3px + ô icon nhạt; sidebar trắng **phẳng kiểu ứng dụng chat, không nhóm đóng/mở** (chốt 13/11/2026): 9 mục, mỗi mục một icon (mixin `navIcon` trong `admin/layout.pug`), mục đang mở nền `--brand-soft` chữ `--brand`; khu nhiều màn (Lịch chiếu, Rạp & phòng, Kinh doanh, Nội dung, Hệ thống) mở bằng tab `.hub-tabs` trên đầu trang.
5. Cam (chữ đen) cho hành động tạo/bán, tối đa một mỗi màn; xanh `#1459A5` cho lọc/xác nhận, liên kết, tab, mục đang chọn.
6. Staff: hai tab trên thanh đầu, không sidebar; nút ẩn/hiện thu gọn cột danh sách suất.
7. Tối giản theo nghĩa "không thừa": không gradient, không emoji, không bóng đổ trên thẻ thường, không màu chỉ để trang trí, không icon tô màu ngẫu nhiên.
8. **Trang khách theo phác thảo của người dùng** (chốt 13/11/2026, UI-Template §3.0 "Bố cục gọn"): header một hàng 64px (không dải chữ trên, không thanh menu xanh đen, không mục "Sự kiện"), hero slide phim có chữ + ảnh ngang + poster, slide khuyến mãi chỉ ảnh, mua vé nhanh dải trắng, lưới phim 6 cột có tim, footer sáng 5 cột. Ưu tiên "nhìn được tổng thể" trên laptop 1366×768. Nút cam chữ đậm. Không quay lại hero 3:1 ló hai bên.
9. **Lịch chiếu = ba tab, một thanh ngữ cảnh, một ngăn Thêm suất** (chốt 14/11/2026 sau phác thảo trên canvas, UI-Template §3.4 "Lịch chiếu"): người dùng thấy bản cũ "rối, khó thao tác" và chọn mức **vừa** (không gộp thành một màn lịch duy nhất, không viết lại toàn bộ). Ba tab Theo ngày (trục giờ theo phòng) · Danh sách (chọn nhiều suất, mở bán/huỷ hàng loạt) · Xếp lịch tuần; ngữ cảnh rạp/phòng/phim/ngày/trạng thái đi theo khi đổi tab; không có hộp "Rạp đang quản lý" hay liên kết chéo. Thêm suất **luôn** qua một ngăn bên cạnh lịch (không hộp thoại chồng lớp), gộp "một suất", "nhiều suất nối tiếp" và "nhiều phim xen kẽ / lần lượt"; sửa hoặc kéo một giờ = ghim, các suất sau tự dời và luôn cách 15 phút dọn phòng. Lưu bằng hai nút "Lưu nháp" và "Lưu và mở bán" (cam, có xác nhận) thay cho ô "Sau khi tạo". Có phím tắt N / ← → / T / Esc, ghi bằng `kbd`. Muốn "hiện đại" thì làm bằng sự gọn, thao tác tại chỗ và phím tắt, không bằng gradient, bóng hay màu trang trí.
   **Cập nhật cùng ngày:** người dùng bỏ dải tab trên trang → Lịch chiếu thành **nhóm trên sidebar** (bấm mở ra ba mục con), trang không breadcrumb/tiêu đề, một thanh ngữ cảnh một hàng rồi một thẻ vừa cửa sổ (cuộn trong thẻ, trang không cuộn). Đây là ngoại lệ có chủ đích của quyết định 4 (sidebar phẳng): chỉ Lịch chiếu là nhóm; muốn đổi các khu khác thì hỏi người dùng. Giờ nhập trong admin dùng ô chữ 24 giờ HH:MM, không dùng `input type=time` (hiện 12 giờ SA/CH).
11. **Lịch chiếu tô màu theo phim** (chốt 15/11/2026, UI-Template §3.5 "Lịch chiếu — màu theo phim"): người dùng thích bảng tuần của kit Pearl vì gọn, và chọn "lấy lớp hình ảnh", không theo sát kit. Nền thẻ suất là màu phim (token `--film-0…7`, phim dùng `mã phim % 8`, giống nhau ở cả ba màn); trạng thái đọc ở viền (liền = mở bán, đứt = nháp, đỏ = mới, xám = đã chiếu). Chức năng của quyết định 9 giữ nguyên. Không quay lại nền thẻ theo trạng thái, không dùng hộp thoại Thêm suất hay ô "Sau khi tạo" của kit.
12. **Xếp lịch: danh sách phim thu vào combobox, kéo phim vào lịch** (chốt 16/11/2026, UI-Template §3.5): giữ nguyên bố cục cột phim bên trái; người dùng chỉ muốn danh sách cuộn gọn vào một nút, bấm xổ ra (lớp nổi, không đẩy trang) và kéo phim từ đó vào lịch. Không chuyển phim lên hàng tab phòng (lần sửa đầu bị chê "hiểu sai ý"). Ngăn Thêm suất: phòng là ô chọn, mỗi dòng phim tự chọn phim; Cả tuần | Theo giờ là khối phân đoạn; tám màu phim cách xa nhau trên vòng màu.

13. **Rạp & phòng gọn, có liên kết, vừa cửa sổ** (chốt 16/11/2026, UI-Template §3.4): trang có tab bỏ breadcrumb, tiêu đề + tab + số kết quả + nút tạo trên một hàng; bộ lọc một dòng; bỏ hộp "Rạp đang quản lý" và **không** thêm dòng đường dẫn (đã thử, người dùng bỏ); số phòng/số ghế là liên kết sang bậc dưới; chỉ bảng cuộn, trang không cuộn. Nhóm sidebar Lịch chiếu nhớ trạng thái mở khi đổi màn.
14. **Thêm phòng = pop-up rồi vào sơ đồ; sửa sơ đồ bằng menu trên nhãn hàng/cột** (chốt 16/11/2026, UI-Template §3.4): mẫu mặc định 4 | lối đi | 4, hai hàng cuối toàn ghế đôi; "4 ghế mỗi bên" chỉ là mẫu, không phải luật. Tên ghế đánh liền, bỏ qua lối đi. Lần 2 (cùng ngày): công cụ là một thanh ngang trên sơ đồ, không cột trái, không nút +/− Hàng/Cột; thêm bằng nút "+" quanh sơ đồ (đầu/cuối hàng = thêm ghế, cạnh hàng/cột = chèn hàng/cột); "Xóa ghế" thay "Lối đi", có phím Delete.
15. **Giá vé = một bảng sửa tại chỗ** (chốt 16/11/2026): loại ghế × Thứ 2–6 / Thứ 7, CN, số tiền là ô nhập; "+ Chia khung giờ" ẩn gọn; chép T2–6 sang T7, CN; không danh sách/bộ lọc/phân trang. Không quay lại bảng tóm tắt + danh sách hai nơi.

16. **Thành viên = bốn tab có liên kết** (17/11/2026, UI-Template §3.4 "Thành viên"): người dùng chê bản đầu là "4 nút vô hồn", màn Hạng là lưới form dài. Hướng đã làm: dải bậc hạng chung trên mọi màn (bấm sang khách / quy tắc), tab có huy hiệu số, Hạng = danh sách + một ngăn sửa, chi tiết khách = thẻ hạng + tab hồ sơ, ô giờ 24 giờ. Không quay lại 5 khối form xếp chồng. Kinh doanh: tab sản phẩm tên "Bắp nước & lưu niệm" (lọc Loại = Lưu niệm) — báo cáo phải gọi đúng tên tab này.
17. **Pearl × Vé cho cả ba vai** (chốt 17/11/2026, UI-Template §3.6): người dùng thấy bản Pearl "hơi gaming và AI", so A (vát có chủ đích) · B (sổ điều hành) · C (vé xem phim) rồi chọn gộp Pearl + C. Mục đang chọn = nền đỏ nhạt + chữ đỏ đậm, không khung đỏ (thay một phần quyết định 4); đường chéo vát = viền; hình vé (cuống, đường xé, mã nét đứt, tem) chỉ ở chỗ có nghĩa vé/số liệu, bảng và form giữ phẳng.
18. **Màn rộng + banner chồng poster** (chốt 17/11/2026, UI-Template §3.6 và §4 Hero): ở scale 100% không để hở hai bên (khung giãn theo màn); banner phim có chồng poster phim kế tiếp chìm phía sau, chuyển mượt, tự chuyển 5 giây; thẻ Ưu đãi / Góc điện ảnh có ảnh = poster dọc 2:3 trên + khung viền nội dung dưới (chỉ ảnh dọc kiểu poster; ảnh ngang thuộc Banner); hai mục nằm cạnh nhau 3 + 3, bài chưa có ảnh dùng bìa chữ cùng khổ; admin Nội dung chia tab theo nơi hiển thị; hai mục nằm cạnh nhau 3 + 3, bài chưa có ảnh dùng bìa chữ cùng khổ; admin Nội dung chia tab theo nơi hiển thị, Admin nhập mô tả ngắn. Ca tại quầy: chênh lệch ghi "Thừa / Thiếu tiền mặt / Khớp két" + số tiền, ghi chú nhân viên là khối riêng.

## 2. Quy trình

### a. Soát hiện trạng
- Ảnh chụp: đọc kỹ từng ảnh. Có app chạy (IntelliJ "CinePrime Local", cổng 8080 — **không tắt**): xem bằng trình duyệt ở 1366×768 (màn khách thêm 375×812).
- Đọc `.pug`/CSS/JS liên quan; chạy lệnh dò ở mục 4.
- Viết ngắn: màn để làm gì, 3–7 vấn đề cụ thể (chỉ chỗ), điều đang tốt cần giữ. Ghi cả lỗi nội dung (cột trùng tên, thiếu đơn vị, dữ liệu lạ).

### b. Trao đổi trước khi vẽ
Người dùng muốn bàn kỹ trước khi mở rộng. Khi hướng còn mở, đưa **2–3 phương án** khác mức độ (nhẹ / vừa / đậm) trên cùng một màn để so, kèm khuyến nghị. Chỉ hỏi quyết định thật sự đổi kết quả; không hỏi lại điều ở mục 1.

### c. Phác thảo
- Vẽ trên canvas thiết kế (Artifact type Design), tiếp tục canvas "CinePrime Admin/Staff — phác thảo" nếu còn; mỗi đợt một trang mới. Không đặt file phác thảo vào repo.
- Đúng token, phông Be Vietnam Pro, logo thật (tải `src/main/resources/static/images/cineprime-mark.svg` làm asset).
- Artboard 1440×960 (màn khách thêm 390×844). Dữ liệu mẫu ghi rõ là ví dụ ("Phim mẫu A", "Khách mẫu 1").

### d. Chốt và ghi lại
Người dùng chọn → cập nhật UI-Template.md (ghi ngày chốt, trạng thái "chưa triển khai mã" nếu chưa làm) và mục 1 của skill này.

### e. Sửa mã (chỉ khi người dùng nói được sửa)
- Pug4J + CSS + JS thuần (AGENTS.md); không framework, không phông mới; màu lấy từ biến `:root`, không hex rời.
- Không đổi nghiệp vụ: giữ ghế 5 phút, khung soát vé, tiền tính ở backend, phân quyền.
- Kiểm chứng trên cổng riêng (8081, schema `cineprime_test`), chụp màn làm bằng chứng, rồi soát theo mục 4. README/Plan chỉ ghi điều đã kiểm chứng.

## 3. Chữ trên màn

- Viết hoa đầu câu; không VIẾT HOA TOÀN BỘ (trừ nhãn "MÀN HÌNH" ở sơ đồ ghế).
- Tiền `170.000 đ` (không "₫"), phần trăm `100%`, giờ `19:42`, ngày `13/11/2026`. Mã vé/đơn phông đơn cách.
- Breadcrumb `Quản trị / Đơn hàng` (khoảng trắng hai bên `/`).
- Nút: động từ + đối tượng ("+ Thêm phim", "Giữ 2 ghế · 5 phút", "Lọc đơn"). Không "OK", không dấu chấm than.
- Từ trạng thái cố định (theo bảng ngữ nghĩa §3.4): Đang hoạt động · Đang chiếu · Sắp chiếu · Đã kết thúc · Đang hiển thị · Đang ẩn · Đã thanh toán · Đang giữ ghế · Chờ MoMo · Chờ đối soát · Thanh toán lỗi · Đã huỷ · Hết hạn · Đã vào · Chưa vào.
- Mô tả cách tính khi số dễ hiểu nhầm ("Theo ngày suất, không theo ngày thanh toán").

## 4. Soát (dùng sau khi làm, hoặc khi được gọi với "soát")

Lệnh dò (đọc kết quả, đừng tin mù quáng):

```bash
grep -nE "gradient" <css>                                # phải rỗng
grep -nE "box-shadow" <css>                              # chỉ hộp thoại, menu nổi
grep -nE "#[0-9a-fA-F]{3,6}" <css> | grep -v ":root"      # hex rời ngoài token
grep -n "₫" <pug> <js>                                   # phải dùng "đ"
grep -nE "border-radius:\s*[1-9]" <css>                   # bo góc: chỉ 50% cho hình tròn thật
grep -nE "background:\s*(#|var)" <css>                    # phần tử vát phải dùng background-color
```

**Nặng — phải sửa:** cụm logo/nút sidebar bị đổi · trạng thái chỉ bằng màu hoặc ô 8px trơn · chữ < 4.5:1 · hơn một nút cam mỗi màn · gradient/emoji/bóng trên thẻ · thay đổi giao diện làm sai nghiệp vụ · (màn khách) vỡ hoặc tràn ngang ở 375px.
**Vừa — nên sửa:** màu không đúng bảng ngữ nghĩa · nút "Thêm mới" đen thay vì cam · thẻ số liệu thiếu vạch/ô icon nghĩa · số không tabular, tiền không căn phải, thiếu đơn vị · mục sidebar thiếu icon, icon tô màu theo mục, quay lại nhóm đóng/mở · dải "—" lặp lại · hex rời.
**Nhẹ:** chữ nút không phải động từ · khoảng cách lệch bội số 4px · breadcrumb thiếu khoảng trắng · thiếu dòng giải thích cách tính.

Mẫu báo cáo:

```
Màn: <tên> (<file>)
Nặng — <vấn đề> · <chỗ: file:dòng hoặc vùng ảnh> · <sửa theo §3.4>
Vừa — ...
Nhẹ — ...
Đã đạt: <2–3 điểm>
```
Mục trống ghi "Không có". Lượt soát không tự sửa mã trừ khi người dùng yêu cầu.
