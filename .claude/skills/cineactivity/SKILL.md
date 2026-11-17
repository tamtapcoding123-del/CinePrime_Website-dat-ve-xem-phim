---
name: cineactivity
description: Skill vẽ Activity Diagram (sơ đồ hoạt động có làn bơi) cho CinePrime ra file draw.io, đúng phong cách ảnh mẫu người dùng gửi và đúng luồng nghiệp vụ thật trong code. Dùng khi người dùng nói "vẽ activity", "sơ đồ hoạt động", "activity diagram", hoặc gọi /cineactivity <tên luồng>.
argument-hint: "<đăng nhập | đặt vé | thanh toán | check-in | lịch chiếu | hủy đơn | quản lý phim> [sửa <yêu cầu>]"
---

# /cineactivity — Activity Diagram CinePrime trên draw.io

Đối số: `$ARGUMENTS`. Trống thì hỏi đúng một câu: vẽ luồng nào (mục 5).

Phong cách lấy **nguyên văn** từ 3 ảnh mẫu người dùng gửi ngày 13/11/2026 (Mua hàng, Chương trình tập luyện, Đăng nhập). Người dùng yêu cầu **đúng và chính xác cao**. Mỗi bước phải có thật trong code CinePrime; không vẽ chức năng chưa có (Google Login, VNPay, hoàn tiền online, đổi điểm, xác thực email khi đăng ký).

## 0. Đầu ra

- **Đầu ra cuối cùng chỉ là ảnh** `.drawio.png` (PNG có nhúng XML draw.io để sửa tiếp) tại `D:\0_JAV102\CinePrime_Project\Sơ đồ\Activity-Diagrams\`: `AD01-dang-ky-dang-nhap.drawio.png`, `AD02-dat-ve`, `AD03-thanh-toan-ve-qr`, `AD04-check-in`, `AD05-quan-ly-lich-chieu`, `AD06-quan-ly-don-huy-don`, `AD07-quan-ly-phim` (cùng đuôi). Vẽ lại thì ghi đè đúng tên cũ.
- **Không tạo file `.py` hay script nào**; lệnh chạy inline. `.drawio` chỉ là file tạm trong thư mục tạm của session và phải xóa sau khi xuất. Cách xuất, cách sửa từ ảnh, cách dọn: [../cinediagram/xuat-anh.md](../cinediagram/xuat-anh.md).
- Mẫu XML đã render kiểm chứng: [mau-activity.drawio](mau-activity.drawio) (luồng Đổi mật khẩu). **Sao chép style từ mẫu**, không tự chế.
- Chú thích "Hình N: Activity …" không nằm trong sơ đồ; khi giao, đề xuất sẵn câu chú thích.
- Sửa sơ đồ: lấy XML từ ảnh (xuat-anh.md §2) ra `.drawio` tạm, chỉ sửa các `mxCell` liên quan bằng Edit, xuất lại đè ảnh cũ.

## 1. Hình thức (bắt buộc — như ảnh mẫu)

| Thành phần | Quy định |
|---|---|
| Khung ngoài | Chữ nhật nét đứt xám `#999999`, bao toàn bộ. |
| Pool | Khung đen; hàng tiêu đề cao 40 ghi **tên luồng**, chữ thường (không đậm), căn giữa. |
| Làn (lane) | Chia dọc ngay dưới tiêu đề, mỗi làn có hàng tên cao 40, chữ thường căn giữa. Làn trái là tác nhân (**Khách hàng** / **Nhân viên** / **Quản trị viên**), làn phải là **Hệ thống**; có làn thứ ba **MoMo** (bên phải) chỉ ở sơ đồ thanh toán. Rộng mỗi làn 360–400. |
| Điểm đầu / cuối | **Đúng một** chấm đen đặc (startState, 24×24) ở làn tác nhân, phía trên cùng; **đúng một** điểm kết thúc (endState, 28×28) ở cuối luồng. Không có điểm kết thúc thứ hai, không có flow final. |
| Hoạt động | Chữ nhật **bo góc**, nền trắng viền đen, chữ 12px căn giữa, 130×46 (dài thì 150–170×46). Tên = động từ + đối tượng, ngắn, đúng từ ngữ giao diện. Đặt đúng làn của bên thực hiện. |
| Nhãn bước | `B1`, `B2`… chữ **đậm** 10px, đặt sát cạnh trái (làn tác nhân) hoặc cạnh phải (làn Hệ thống) của hoạt động; không đè đường nối. Đánh số theo thứ tự luồng chính; nút rẽ nhánh cũng được đánh số. |
| Rẽ nhánh / hợp nhánh | Hình **thoi** trắng viền đen 36×36. Điều kiện ghi chữ thường cạnh đường ra (ví dụ "Hợp lệ" / "Không hợp lệ", "Đúng" / "Sai", "Còn hạn" / "Hết hạn"), không ngoặc vuông. Mọi nhánh kết thúc khác nhau phải **gộp lại bằng một hình thoi hợp nhánh** trước điểm kết thúc (như ảnh "Chương trình tập luyện"). Nhánh lỗi thường **quay lại** bước nhập. |
| Luồng | Đường đen **vuông góc, góc vuông** (không bo), **mũi tên hở**. Đi ngang khi chuyển làn, đi dọc trong làn; không cắt ngang hoạt động khác; hạn chế cắt nhau. |
| Màu | Chỉ đen/trắng/xám khung ngoài, Helvetica. Không thanh fork/join, ghi chú hay icon (ảnh mẫu không dùng). |

## 2. Style draw.io (sao chép nguyên văn)

```
KHUNG NGOÀI rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=#999999;dashed=1;dashPattern=8 4;connectable=0;
POOL/LANE   swimlane;html=1;startSize=40;horizontal=1;fontStyle=0;fontSize=13;fontFamily=Helvetica;fontColor=#000000;fillColor=#FFFFFF;swimlaneFillColor=#FFFFFF;strokeColor=#000000;collapsible=0;connectable=0;
BẮT ĐẦU     ellipse;html=1;shape=startState;fillColor=#000000;strokeColor=#000000;
KẾT THÚC    ellipse;html=1;shape=endState;fillColor=#000000;strokeColor=#000000;
HOẠT ĐỘNG   rounded=1;whiteSpace=wrap;html=1;arcSize=14;fillColor=#FFFFFF;strokeColor=#000000;fontColor=#000000;fontSize=12;fontFamily=Helvetica;
RẼ NHÁNH    rhombus;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#000000;
NHÃN BƯỚC   text;html=1;align=center;verticalAlign=middle;fontSize=10;fontStyle=1;fontColor=#000000;
LUỒNG       edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;endArrow=open;endFill=0;endSize=8;strokeColor=#000000;fontColor=#000000;fontSize=12;labelBackgroundColor=none;  + exitX/exitY/entryX/entryY
```

- Pool và lane đều `parent="1"`, tọa độ tuyệt đối: pool `(x, y, W, H)`; lane i: `x + i·w, y + 40, w, H − 40`. Mọi nút và luồng `parent="1"`.
- Thứ tự cell: khung ngoài → pool → lane → nút → nhãn bước → luồng.
- Hướng nối như mẫu: cùng làn đi xuống `exitX=0.5;exitY=1 → entryX=0.5;entryY=0`; sang làn khác đi ngang `exitX=1;exitY=0.5 → entryX=0;entryY=0.5` (hoặc ngược lại); quay về làn tác nhân `exitX=0.5;exitY=1 → entryX=0.5;entryY=0`; vòng lặp lỗi đi lên `exitX=0.5;exitY=0 → entryX=1;entryY=0.5`.
- Nhãn điều kiện: `<mxPoint x="0" y="-12" as="offset"/>` cho đường ngang, `x="-22" y="0"` cho đường dọc.
- Bố cục: hàng cách nhau 70–90px, hoạt động căn giữa làn hoặc lệch để đường không đè; nhánh lỗi đặt bên phải hình thoi, nhánh chính đi thẳng xuống.

## 3. Quy trình

1. Đọc mục 5 của luồng cần vẽ. Nghi ngờ chi tiết nào thì kiểm lại nguồn ghi ở mục đó (không quét toàn bộ dự án).
2. Viết `.drawio` **tạm** trong thư mục tạm của session theo mục 1–2 và mẫu. Đếm lại: **1 start, 1 end**, mọi nhánh đều tới được end, không nút cụt.
3. Xuất ảnh vào `Sơ đồ\Activity-Diagrams\` bằng khối lệnh inline ở xuat-anh.md §1, mở ảnh bằng Read để soát. Sai thì sửa `.drawio` tạm và xuất lại.
4. Dọn: xóa `.drawio` tạm, chạy kiểm tra ở xuat-anh.md §3. Giao: đường dẫn ảnh, câu chú thích, và các điểm đơn giản hóa đã chọn (nếu có).

### Bảng soát

- [ ] Đúng 1 điểm bắt đầu (làn tác nhân) và 1 điểm kết thúc; nhánh khác nhau gộp bằng hình thoi trước điểm kết thúc.
- [ ] Mỗi hoạt động nằm đúng làn bên thực hiện; Hệ thống không "nhập", tác nhân không "kiểm tra/lưu".
- [ ] Mọi hình thoi rẽ nhánh có điều kiện trên **mọi** đường ra; nhánh lỗi quay lại hoặc gộp.
- [ ] Bước, điều kiện, con số (5 phút, 1–8 ghế, 60/30 phút, 15 phút vệ sinh…) khớp mục 5 và code.
- [ ] Đường vuông góc mũi tên hở, không cắt hoạt động; nhãn B không bị đè; chỉ đen trắng.
- [ ] Ảnh đủ khung, không bị cắt; `Sơ đồ\Activity-Diagrams\` chỉ có `.drawio.png`, không còn `.drawio`, `.py` hay script tạm nào.

## 4. Nhận xét danh sách luồng (đã thống nhất đề xuất 13/11/2026)

Nên vẽ **6 luồng cốt lõi** AD01–AD06. AD07 Quản lý phim là CRUD ít rẽ nhánh, đã thể hiện ở Use case. Chỉ vẽ khi đề yêu cầu đủ phía Admin. Tên luồng "Quản lý đơn đặt vé và hủy vé" chỉnh thành **"Quản lý đơn đặt vé và hủy đơn"**: CinePrime **không** hủy/hoàn tiền vé đã thanh toán online. Chỉ đơn **chờ thanh toán** được hủy (Khách hàng hoặc Quản trị viên). Riêng vé bán tại quầy có thể do Nhân viên hủy và hoàn tiền mặt trong 15 phút. Luồng này chỉ vẽ khi người dùng yêu cầu.

## 5. Đặc tả luồng (nguồn: code ngày 13/11/2026)

### AD01 Đăng ký và đăng nhập — làn Người dùng | Hệ thống
Nguồn: `auth/AccountService.register`, `AccountController`, `SecurityConfig` (successHandler, rate limit).
1. Người dùng: Truy cập Đăng nhập / Đăng ký → thoi "Đã có tài khoản?".
2. **Chưa**: Chọn Đăng ký → HT: Hiển thị form đăng ký → ND: Nhập email, họ tên, mật khẩu → HT thoi Kiểm tra (email hợp lệ và chưa được dùng, họ tên ≤ 100 ký tự, mật khẩu ≥ 10 ký tự) → *Không hợp lệ*: Thông báo lỗi → quay lại form đăng ký; *Hợp lệ*: Tạo tài khoản Khách hàng (mật khẩu được mã hóa) → Chuyển đến form đăng nhập. **Không** có mã xác thực email, **không** tự đăng nhập.
3. **Có**: Chọn Đăng nhập → HT: Hiển thị form đăng nhập (nhánh đăng ký gộp vào đây).
4. ND: Nhập email, mật khẩu → HT thoi Kiểm tra (đúng thông tin, tài khoản không bị khóa, không quá số lần thử) → *Sai*: Thông báo lỗi → quay lại form đăng nhập; *Đúng*: Tạo phiên đăng nhập → Chuyển trang theo vai trò (Khách hàng → trang đang đặt vé hoặc Tài khoản; Nhân viên → Soát vé; Quản trị viên → Quản trị) → **kết thúc**.

### AD02 Đặt vé xem phim — làn Khách hàng | Hệ thống (dừng khi chuyển sang thanh toán)
Nguồn: `booking/BookingService.hold/reserve`, `BookingController`, trang `seats.pug`, `order.pug`.
1. KH: Chọn phim → HT: Hiển thị chi tiết phim và lịch chiếu → KH: Chọn rạp, ngày, suất chiếu.
2. HT thoi "Đã đăng nhập?" → *Chưa*: Chuyển đến đăng nhập, quay lại suất đã chọn sau khi đăng nhập (gộp lại).
3. HT: Hiển thị sơ đồ ghế (trống / đang giữ / đã bán / bảo trì, giá từng loại) → KH: Chọn ghế (1–8 ghế, ghế đôi chọn đủ cặp).
4. HT thoi Kiểm tra: suất còn mở bán và còn ≥ 20 phút tới giờ chiếu; ghế còn trống, không bảo trì; khách không còn đơn đang giữ ghế khác → *Không hợp lệ*: Thông báo lý do → quay lại sơ đồ ghế; *Hợp lệ*: Giữ ghế 5 phút, tạo đơn chờ thanh toán.
5. HT: Hiển thị đơn hàng (ghế, giá, đồng hồ giữ ghế) → KH: Chọn combo, nhập voucher (tùy chọn) → HT: Kiểm tra voucher, tính lại tổng tiền (máy chủ tính; không gia hạn giữ ghế) → KH: Chọn Thanh toán → HT thoi "Còn hạn giữ ghế?" → *Hết hạn*: Trả ghế, đơn hết hạn; *Còn hạn*: Chuyển sang thanh toán (AD03) → hợp nhánh → **kết thúc**.

### AD03 Thanh toán và phát hành vé QR — làn Khách hàng | Hệ thống | MoMo
Nguồn: `payment/MomoService.create/ipn/query`, `BookingService.preparePayment/paymentResult/issue`.
1. KH: Chọn thanh toán MoMo → HT thoi Kiểm tra đơn (còn hạn giữ ghế, tổng ≥ 1.000đ) → *Không hợp lệ*: Thông báo lỗi → hợp nhánh cuối.
2. *Hợp lệ*: HT: Tạo giao dịch, gửi yêu cầu thanh toán (có chữ ký) → MoMo: Tạo liên kết thanh toán → HT: Chuyển khách sang trang MoMo → KH: Xác nhận thanh toán trên MoMo → MoMo: Gửi kết quả thanh toán (IPN) về hệ thống.
3. HT: Xác thực chữ ký, merchant, mã đơn, số tiền (xử lý lặp lại an toàn) → thoi Kết quả → *Thất bại*: Ghi nhận thanh toán thất bại (ghế giữ đến hết hạn) → hợp nhánh; *Thành công* → thoi "Đơn còn hạn giữ ghế?" → *Hết hạn*: Ghi nhận để đối soát, **không phát vé** → hợp nhánh; *Còn hạn*: Phát hành vé (mỗi ghế một mã QR), chuyển ghế sang đã bán → Cộng điểm, gửi email và thông báo xác nhận → hợp nhánh.
4. Sau hợp nhánh: HT: Hiển thị trạng thái đơn (trang trả về chỉ đọc trạng thái) → KH: Xem vé QR / tải vé PDF (nếu thành công) → **kết thúc**.
Ghi chú (không vẽ, nêu khi giao): đơn 0đ nhờ voucher hoàn tất không qua MoMo; khách có thể bấm kiểm tra lại trạng thái (HT truy vấn MoMo) khi chưa có kết quả.

### AD04 Quét mã QR và Check-in — làn Nhân viên | Hệ thống
Nguồn: `staff/CheckInService.lookup/checkIn/eligibility`, `StaffController`.
1. NV: Chọn suất chiếu đang soát → HT: Hiển thị suất tại rạp được phân công và danh sách vé đã vào / chưa vào.
2. NV: Quét mã QR bằng camera hoặc nhập mã vé / mã đơn → HT thoi "Tìm thấy vé thuộc rạp phụ trách?" → *Không*: Thông báo lỗi → quay lại bước quét.
3. *Có*: HT thoi Kiểm tra điều kiện (đúng suất đang soát, vé chưa sử dụng, vé và đơn còn hiệu lực, trong khung 60 phút trước đến 30 phút sau giờ chiếu) → *Không đạt*: Hiển thị lý do (đã sử dụng / sai suất / hết hiệu lực / ngoài khung giờ) → quay lại bước quét.
4. *Đạt*: HT: Hiển thị thông tin vé (phim, phòng, ghế) → NV: Xác nhận cho vào → HT: Đánh dấu vé đã sử dụng (mỗi QR một lần), ghi nhật ký → Cập nhật số khách đã vào → **kết thúc**.

### AD05 Quản lý lịch chiếu — làn Quản trị viên | Hệ thống
Nguồn: `admin/ShowtimeBatches`, `ShowtimeRules`, `ShowtimeExcel`, `ShowtimeBatchApi` (preview/commit/import-preview).
1. QTV: Chọn rạp và tuần (Thứ Hai–Chủ nhật) → HT: Hiển thị lịch tuần theo phòng.
2. QTV: Thêm suất chiếu (phim, phòng, ngày, giờ bắt đầu, ngôn ngữ, định dạng, phụ thu) trên lịch hoặc nhập file Excel.
3. HT: Tính giờ kết thúc (thời lượng phim + 15 phút vệ sinh) → thoi Kiểm tra (suất trong tương lai; ngày nằm trong khoảng khởi chiếu – kết thúc của phim; phim / phòng còn hoạt động; phim có thể loại hoạt động; phòng có ghế; không trùng giờ trong phòng; đủ bảng giá) → *Có lỗi*: Hiển thị dòng lỗi → quay lại bước 2.
4. *Hợp lệ*: HT: Hiển thị bản xem trước → QTV: Xác nhận lưu → HT thoi "Dữ liệu còn khớp bản xem trước?" → *Không*: Yêu cầu kiểm tra lại → quay lại bản xem trước; *Có*: Lưu toàn bộ suất (tất cả hoặc không) → Cập nhật lịch chiếu → **kết thúc**.

### AD06 Quản lý đơn đặt vé và hủy đơn — làn Quản trị viên | Hệ thống
Nguồn: `admin/AdminController` (orders, `/orders/{id}/cancel`, `/payments/{id}/verify`), `BookingService.cancelUnpaid/release`, `MomoService.query`.
1. QTV: Tìm, lọc đơn (mã đơn, trạng thái, ngày) → HT: Hiển thị danh sách đơn → QTV: Chọn đơn → HT: Hiển thị chi tiết (ghế, combo, voucher, thanh toán, vé).
2. HT thoi "Trạng thái đơn?":
   - *Chờ thanh toán*: QTV: Chọn Hủy đơn → HT: Hủy đơn, trả ghế về trống, hoàn lượt voucher → Cập nhật trạng thái Đã hủy.
   - *Có giao dịch MoMo cần xác minh*: QTV: Chọn Kiểm tra giao dịch → HT: Truy vấn MoMo, cập nhật trạng thái thanh toán / đánh dấu đối soát.
   - *Đã thanh toán / Đã hủy / Hết hạn*: Chỉ xem (không hủy, không hoàn tiền online).
3. Hợp nhánh → **kết thúc**.

### AD07 Quản lý phim (tùy chọn) — làn Quản trị viên | Hệ thống
Nguồn: `admin/AdminService` (movies), `movie-form.pug`.
1. QTV: Chọn Thêm phim hoặc Sửa phim → HT: Hiển thị form phim có xem trước.
2. QTV: Nhập tên, đường dẫn, thời lượng, độ tuổi, ngày khởi chiếu, ngày kết thúc, thể loại, đạo diễn / diễn viên, poster / banner / trailer.
3. HT thoi Kiểm tra (bắt buộc tên và hai ngày; ngày kết thúc ≥ ngày khởi chiếu; thời lượng 1–600 phút; đường dẫn chỉ chữ thường, số, gạch ngang; ảnh/video đúng loại và dung lượng; không rút khoảng ngày khi còn suất nằm ngoài; không đổi thời lượng khi còn suất sắp tới) → *Không hợp lệ*: Thông báo lỗi → quay lại form.
4. *Hợp lệ*: HT: Lưu phim, trạng thái Sắp chiếu / Đang chiếu / Đã kết thúc tự suy theo ngày → Hiển thị danh sách phim → **kết thúc**.
