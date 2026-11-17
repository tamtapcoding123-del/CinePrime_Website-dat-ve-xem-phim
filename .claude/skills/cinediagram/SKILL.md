---
name: cinediagram
description: Skill vẽ sơ đồ phân tích thiết kế cho CinePrime ra file draw.io (.drawio) — Use case (tổng quát, Admin, Staff, Customer, Đặt vé, Thanh toán, Quản lý lịch chiếu, Quản lý vé và Check-in, hoặc chức năng khác) và Sơ đồ màn hình (Sitemap). Dùng khi người dùng nói "vẽ usecase", "sơ đồ use case", "sitemap", "sơ đồ màn hình", "draw.io", hoặc gọi /cinediagram <tên sơ đồ>.
argument-hint: "<tổng quát | admin | staff | customer | đặt vé | thanh toán | lịch chiếu | check-in | sitemap | tên chức năng khác> [sửa <yêu cầu>]"
---

# /cinediagram — Use case và Sitemap CinePrime trên draw.io

Đối số: `$ARGUMENTS`. Trống thì hỏi đúng một câu: vẽ sơ đồ nào.

Phong cách lấy **nguyên văn** từ bộ ảnh mẫu người dùng gửi ngày 13/11/2026 (đồ án WellFitness, Hình 5–12). Người dùng yêu cầu vẽ **giống hệt** phong cách đó. Không tự thêm màu, bóng, icon, chú thích, ghi chú hay ký hiệu UML ngoài những gì ghi ở đây.

## 0. Đầu ra

- **Đầu ra cuối cùng chỉ là ảnh PNG** (có nhúng XML draw.io để sửa tiếp) tại `D:\0_JAV102\CinePrime_Project\Sơ đồ\Usecase\`. **Không tạo file `.py` hay script nào**; lệnh chạy inline. `.drawio` chỉ là file tạm trong thư mục tạm của session, xuất ảnh xong phải xóa. Cách xuất, cách sửa từ ảnh và cách dọn: [xuat-anh.md](xuat-anh.md). Bắt buộc đọc trước khi giao.
- Tên ảnh: `UC01-tong-quat.png`, `UC02-admin.png`, `UC03-staff.png`, `UC04-customer.png`, `UC05-dat-ve.png`, `UC06-thanh-toan.png`, `UC07-quan-ly-lich-chieu.png`, `UC08-quan-ly-ve-check-in.png`, `SM01-sitemap.png`; sơ đồ khác lấy số tiếp theo. Vẽ lại thì ghi đè đúng tên cũ.
- Chú thích "Hình N: …" **không** nằm trong sơ đồ (người dùng gõ trong Word). Khi giao, đề xuất sẵn câu chú thích.
- Sửa sơ đồ đã có: lấy XML từ ảnh (xuat-anh.md §2) ra `.drawio` tạm, sửa đúng các `mxCell` liên quan bằng Edit, xuất lại đè ảnh cũ. Không vẽ lại toàn bộ.
- Mẫu XML chuẩn đã render kiểm chứng: [mau-usecase.drawio](mau-usecase.drawio), [mau-sitemap.drawio](mau-sitemap.drawio). Sao chép style từ đó, không tự chế style.

## 1. Hình thức Use case (bắt buộc)

| Thành phần | Quy định |
|---|---|
| Khung ngoài | Chữ nhật **nét đứt xám** `#999999`, không nền, bao toàn bộ (cả hệ thống và tác nhân), cách mép trong ~20px. |
| Hệ thống | Chữ nhật **đen nét liền**, nền trắng. Tên hệ thống/phân hệ **in đậm, căn giữa, sát mép trên bên trong** (16px). Mọi chức năng nằm **bên trong**. |
| Chức năng | **Ellipse** nền trắng viền đen, chữ đen thường 13px căn giữa, tự xuống dòng. Kích thước chuẩn 160×90 (tổng quát/quản lý có thể 140×70; tối thiểu 120×60). Tên dài thì nới rộng, không thu chữ. |
| Tác nhân | **Người que** (`shape=umlActor`, 30×60), tên đặt **dưới** hình, nằm **ngoài** khung hệ thống, ở mép trái hoặc phải. |
| Liên kết tác nhân | **Đường đen liền, thẳng, không mũi tên**, nối từ tác nhân vào ellipse. |
| include / extend | **Đường đen đứt đoạn, mũi tên hở** (`endArrow=open`), nhãn chữ thường `<<include>>` / `<<extend>>` cỡ 11, nằm **cạnh** đường (lệch lên trên hoặc xuống dưới bằng `offset`), không nền, không đè lên đường. Chỉ nối **giữa các chức năng bên trong hệ thống**. |
| Màu, phông | Chỉ đen/trắng (và xám cho khung ngoài). Helvetica. Không gradient, bóng, bo góc, tô màu, icon. |
| Đường nối | Luôn thẳng (không `edgeStyle` gấp khúc), không cắt ngang ellipse khác, hạn chế cắt nhau. |

## 2. Ngữ nghĩa Use case

- **include**: chức năng gốc **bắt buộc** gọi chức năng con. Mũi tên **từ chức năng gốc → chức năng được include** (Hình 7, 9, 11). Mọi chức năng cần tài khoản trong sơ đồ chi tiết include `Đăng nhập`.
- **extend**: chức năng **tùy chọn, có điều kiện**. Mũi tên **từ chức năng mở rộng → chức năng gốc** (chuẩn UML, như Hình 8). Ảnh mẫu Hình 9–10 vẽ ngược chiều; skill thống nhất theo chuẩn UML để không bị bắt lỗi — chỉ đổi khi người dùng yêu cầu.
- Không dùng generalization, không vẽ quan hệ giữa hai tác nhân, không vẽ include/extend trong sơ đồ tổng quát và sơ đồ theo vai trò (như Hình 5–6).
- Tên chức năng: **động từ + đối tượng**, viết hoa chữ đầu, tiếng Việt có dấu, ≤ 6 từ; gộp thao tác CRUD thành "Thêm, sửa, xóa …" như ảnh mẫu. Chỉ dùng từ ở mục 5; chức năng phải **có thật** trong CinePrime (không vẽ Google Login, VNPay, hoàn tiền, đổi điểm, gợi ý phim — chưa làm).
- Tác nhân phụ là hệ thống ngoài (chỉ `MoMo`) được vẽ người que ở **mép phải**, chỉ trong sơ đồ có giao tiếp thật với nó.

## 3. Bố cục chuẩn (tọa độ tính bằng px, lưới 10)

- **A — Tổng quát (Hình 5)**: hệ thống "CinePrime Website" ở giữa; tác nhân điều hành (Quản trị viên, Nhân viên) bên **trái**, Khách hàng bên **phải**, đặt ngang giữa nhóm chức năng của mình. Ellipse xếp thành 1 cột (≤ 8) hoặc 2 cột (9–14), cách dọc 20px; chức năng dùng chung đặt giữa. Trang rộng ~1000–1100.
- **B — Theo vai trò (Hình 6)**: một tác nhân bên trái, giữa chiều cao; ellipse xếp 1 cột sát nửa phải khung hệ thống (≤ 7) hoặc 2 cột so le (8–12). Tên hệ thống = tên phân hệ ("Quản trị", "Nhân viên", "Khách hàng").
- **C — Chi tiết chức năng (Hình 7–11)**: tác nhân trái (hoặc phải nếu bên trái đã dày), chức năng gốc ở cột 1 trong khung, các chức năng include/extend tỏa sang cột 2 bên phải, cách dọc 90–110px. Chức năng có liên kết tác nhân riêng (như "Đánh giá" Hình 7) đặt dưới chức năng gốc, cột 1. Tên hệ thống = tên chức năng gốc.
- Kích thước khung: khung ngoài = hệ thống + tác nhân + lề 20–40px; khung hệ thống chừa ≥ 40px quanh ellipse; nhãn tiêu đề không đè ellipse.
- Nhãn include/extend: đường gần ngang → `offset` y = −14 (trên) hoặc +14 (dưới); đường gần dọc → x = +45 (bên phải đường).

## 4. Style draw.io (sao chép nguyên văn)

```
KHUNG NGOÀI  rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=#999999;dashed=1;dashPattern=8 4;connectable=0;
HỆ THỐNG     rounded=0;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#000000;verticalAlign=top;align=center;fontStyle=1;fontSize=16;fontColor=#000000;fontFamily=Helvetica;spacingTop=6;connectable=0;
CHỨC NĂNG    ellipse;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#000000;fontColor=#000000;fontSize=13;fontFamily=Helvetica;
TÁC NHÂN     shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;outlineConnect=0;fillColor=#FFFFFF;strokeColor=#000000;fontColor=#000000;fontSize=13;fontFamily=Helvetica;
LIÊN KẾT     endArrow=none;startArrow=none;rounded=0;strokeColor=#000000;
INCLUDE/EXT  endArrow=open;endFill=0;endSize=10;startArrow=none;dashed=1;dashPattern=4 3;rounded=0;strokeColor=#000000;fontColor=#000000;fontSize=11;fontFamily=Helvetica;labelBackgroundColor=none;
             value="&lt;&lt;include&gt;&gt;" hoặc "&lt;&lt;extend&gt;&gt;" — cạnh KHÔNG có html=1 (nếu có, << >> bị hiểu là thẻ HTML)
SM KHUNG     rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=#666666;connectable=0;
SM TRANG     rounded=0;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#000000;fontColor=#000000;fontStyle=1;fontSize=11;fontFamily=Helvetica;
SM CÂY       edgeStyle=orthogonalEdgeStyle;rounded=0;endArrow=none;startArrow=none;strokeColor=#000000;exitX=0.5;exitY=1;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;
SM DỌC/NGANG endArrow=none;startArrow=none;rounded=0;strokeColor=#000000; + exit/entry đúng cạnh
```

Cấu trúc file: `<mxfile><diagram name="…"><mxGraphModel … pageWidth pageHeight vừa khung ngoài + 20><root>` → `mxCell id="0"`, `id="1" parent="0"` → khung ngoài → hệ thống → tác nhân → chức năng → cạnh. Mọi đỉnh `parent="1"`, tọa độ tuyệt đối. Id ngắn, có nghĩa (`a-kh`, `uc-dat-ve`, `e-…`). Thứ tự này giữ khung ngoài/hệ thống nằm dưới cùng.

## 5. Danh mục CinePrime (từ ngữ chuẩn — lấy từ Plan.md §2, README §5 và menu thật)

Tác nhân: **Khách hàng**, **Nhân viên**, **Quản trị viên**; hệ thống ngoài **MoMo**.

- **Khách hàng**: Đăng ký; Đăng nhập; Đăng xuất; Quên mật khẩu; Xem phim; Tìm kiếm, lọc phim; Xem lịch chiếu; Xem rạp, giá vé; Xem khuyến mãi, bài viết; Đặt vé; Chọn suất chiếu; Chọn ghế; Chọn combo; Áp dụng voucher; Thanh toán; Thanh toán qua MoMo; Xem kết quả thanh toán; Xem vé của tôi; Tải vé PDF; Hủy đơn chờ thanh toán; Cập nhật hồ sơ; Đổi mật khẩu; Yêu thích phim; Đánh giá phim; Đăng ký nhắc phim sắp chiếu; Xem thông báo; Xem điểm thưởng.
- **Nhân viên** (chỉ rạp được phân công): Xem suất chiếu trong ngày; Check-in vé; Quét mã QR; Nhập mã vé thủ công; Xem danh sách đã/chưa vào; Mở ca bán vé; Bán vé tại quầy; Thu tiền mặt; In vé; Hủy đơn tại quầy (trong 15 phút); Đóng ca bán vé.
- **Quản trị viên** (menu thật): Thống kê, báo cáo; Xuất báo cáo CSV; Quản lý đơn hàng; Đối soát thanh toán; Quản lý phim; Quản lý thể loại, đạo diễn, diễn viên; Quản lý lịch chiếu; Quản lý rạp; Quản lý phòng chiếu; Quản lý ghế, bảo trì; Quản lý bảng giá; Quản lý combo; Quản lý voucher; Quản lý banner; Quản lý bài viết, ưu đãi, chính sách; Duyệt bình luận; Quản lý người dùng; Phân quyền, khóa tài khoản; Quản lý ca nhân viên; Xem nhật ký hoạt động.
- **Lịch chiếu (Admin)**: Xem lịch theo ngày; Xếp lịch theo tuần; Thêm, sửa, ngừng suất chiếu; Thêm suất hàng loạt; Nhập lịch từ Excel; Xuất lịch ra Excel; Kiểm tra trùng phòng (tự động khi lưu, gồm thời gian dọn phòng).
- **MoMo**: nhận yêu cầu thanh toán; gửi kết quả thanh toán (IPN); trả trạng thái giao dịch (query).

Nếu cần chức năng không có ở đây: kiểm tra `AdminController.nav`, `layout.pug`, `mixins.pug` (menu Staff) hoặc Plan.md §2 trước khi vẽ; không có thì không vẽ.

## 6. Đặc tả 9 sơ đồ đã đặt (điểm xuất phát — được điều chỉnh khi người dùng yêu cầu)

1. **UC01 Tổng quát** (bố cục A, hệ thống "CinePrime Website"): Khách hàng — Xem phim, lịch chiếu; Đặt vé; Thanh toán; Quản lý tài khoản cá nhân; Đánh giá phim. Nhân viên — Check-in vé; Bán vé tại quầy. Quản trị viên — Quản lý phim; Quản lý lịch chiếu; Quản lý rạp, phòng chiếu; Quản lý đơn hàng; Quản lý combo, voucher; Quản lý người dùng; Thống kê, báo cáo. Chức năng chung "Đăng nhập" không vẽ ở đây (như Hình 5).
2. **UC02 Admin** (B, "Quản trị"): Thống kê, báo cáo; Quản lý đơn hàng; Quản lý phim; Quản lý lịch chiếu; Quản lý rạp, phòng chiếu; Quản lý bảng giá; Quản lý combo, voucher; Quản lý nội dung, banner; Duyệt bình luận; Quản lý người dùng; Quản lý ca nhân viên; Xem nhật ký hoạt động.
3. **UC03 Staff** (B, "Nhân viên"): Xem suất chiếu trong ngày; Check-in vé; Xem danh sách đã/chưa vào; Mở ca bán vé; Bán vé tại quầy; In vé; Hủy đơn tại quầy; Đóng ca bán vé.
4. **UC04 Customer** (B, "Khách hàng"): Đăng ký, đăng nhập; Xem phim, lịch chiếu; Đặt vé; Thanh toán; Xem vé của tôi; Cập nhật hồ sơ; Yêu thích, đánh giá phim; Xem thông báo; Xem điểm thưởng.
5. **UC05 Đặt vé** (C): Khách hàng — Đặt vé. include → Đăng nhập, Chọn suất chiếu, Chọn ghế, Thanh toán. extend → Chọn combo, Áp dụng voucher (mũi tên về Đặt vé). Khách hàng — Hủy đơn chờ thanh toán (extend → Đặt vé).
6. **UC06 Thanh toán** (C, MoMo bên phải): Khách hàng — Thanh toán. include → Đăng nhập, Xác nhận đơn hàng, Thanh toán qua MoMo. Thanh toán qua MoMo nối MoMo; extend Xem kết quả thanh toán → Thanh toán; Nhận vé QR, PDF (extend → Xem kết quả thanh toán). Quản trị viên chỉ thêm khi người dùng muốn vẽ Đối soát thanh toán.
7. **UC07 Quản lý lịch chiếu** (C, Quản trị viên): Quản lý lịch chiếu include → Đăng nhập; extend → Xem lịch theo ngày, Xếp lịch theo tuần, Thêm, sửa, ngừng suất chiếu, Nhập lịch từ Excel, Xuất lịch ra Excel. Thêm, sửa, ngừng suất chiếu include → Kiểm tra trùng phòng.
8. **UC08 Quản lý vé và Check-in** (C, Nhân viên trái, Quản trị viên phải): Nhân viên — Check-in vé include → Đăng nhập, Tra cứu vé; extend → Quét mã QR, Nhập mã vé thủ công (về Tra cứu vé); Nhân viên — Xem danh sách đã/chưa vào. Quản trị viên — Quản lý đơn hàng include → Đăng nhập; Hủy đơn hàng extend → Quản lý đơn hàng.
9. **SM01 Sitemap** (mục 7): giao diện khách.

## 7. Sitemap (Hình 12)

- Khung ngoài chữ nhật **nét liền xám** `#666666`. Mỗi trang là **chữ nhật** viền đen, chữ **đậm** 11px căn giữa, 100–120×45 (chữ dài thì rộng 120 hoặc chèn `&lt;br&gt;` chủ động). Không ellipse, không màu, không mũi tên.
- Gốc "Trang chủ" trên cùng giữa. Cấp 2 một hàng ngang, nối bằng **cây vuông góc** (SM CÂY, thanh ngang chung). Cấp sâu hơn xếp **dọc** dưới trang cha bằng đường thẳng dọc. Trang "Danh mục/lọc" ngang hàng trang "Danh sách" nối bằng **đường ngang** (như Hình 12).
- Mặc định giao diện khách theo menu thật: Trang chủ → Phim (Danh sách phim ↔ Lọc thể loại, ngày, tuổi; ↓ Chi tiết phim ↓ Chọn ghế ↓ Combo, voucher, thanh toán ↓ Kết quả thanh toán, vé), Lịch chiếu, Khuyến mãi, Góc điện ảnh (↓ Bài viết), Rạp / Giá vé (↓ Chi tiết rạp), Tài khoản (Vé của tôi, Hồ sơ, Yêu thích, Thông báo, Điểm thưởng), Đăng nhập / Đăng ký (↓ Quên mật khẩu), Giới thiệu / Chính sách / Liên hệ. Sitemap Admin/Staff chỉ vẽ khi được yêu cầu, lấy từ menu `AdminController.nav`.

## 8. Quy trình

1. Xác định sơ đồ, bố cục A/B/C/Sitemap, nội dung theo mục 5–6. Không quét lại mã nếu mục 5 đã đủ.
2. Viết file `.drawio` **tạm** trong thư mục tạm của session (style mục 4, tọa độ tính tay theo mục 3).
3. Xuất ảnh vào `Sơ đồ\Usecase\` bằng lệnh inline ở [xuat-anh.md](xuat-anh.md) §1 (lỗi `[xml]` = XML hỏng → sửa trước). Mở ảnh bằng Read để soát.
4. Soát theo bảng dưới; sai thì sửa `.drawio` tạm và xuất lại. Xong thì xóa file tạm và chạy kiểm tra dọn dẹp ở xuat-anh.md §3. Giao: đường dẫn ảnh, câu chú thích "Hình N: …", và các giả định đã đưa ra.

### Bảng soát (mọi mục phải đạt)

- [ ] Khung ngoài nét đứt xám bao trọn; khung hệ thống đen, tiêu đề đậm trên cùng, không đè ellipse.
- [ ] Mọi chức năng là ellipse trong khung hệ thống; mọi tác nhân là người que ngoài khung, tên ở dưới.
- [ ] Tác nhân ↔ chức năng: đường liền, không mũi tên. include/extend: nét đứt, mũi tên hở, nhãn đúng chiều mục 2, nhãn không đè đường/ellipse.
- [ ] Không màu, không đường gấp khúc (trừ cây sitemap), không đường cắt qua ellipse, chữ không tràn ellipse.
- [ ] Tên chức năng đúng mục 5, động từ + đối tượng, chức năng có thật; không lặp chức năng.
- [ ] XML hợp lệ, mọi cạnh có `source`/`target` trỏ id tồn tại; ảnh xuất đủ khung, không bị cắt.
- [ ] Trong `Sơ đồ\Usecase\` chỉ có ảnh `.png`; không còn file `.drawio`, `.py` hay script nào được tạo ra.
