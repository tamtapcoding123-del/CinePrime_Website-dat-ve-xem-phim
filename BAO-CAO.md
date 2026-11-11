# Ghi chú báo cáo — CinePrime

Nhánh `ghi-chu` chỉ chứa nội dung báo cáo; không đưa vào `README.MD` của nhánh `main`.

## 1. Mô hình công nghệ (Tech stack)

- Ngôn ngữ phía máy chủ: Java 25.
- Framework: Spring Boot MVC, đóng gói một file JAR chạy được.
- Giao diện: Pug4J (render HTML phía máy chủ), CSS thuần.
- Truy cập dữ liệu: JPA/Hibernate (`ddl-auto: validate`), migration bằng Flyway.
- Cơ sở dữ liệu: MySQL 8.
- Bảo mật: Spring Security, session, CSRF, băm mật khẩu.
- Thanh toán: MoMo (captureWallet, IPN, truy vấn, xác thực HMAC).
- Tiện ích: PDFBox (vé PDF), mã QR, ZXing (quét QR bằng camera), email outbox.
- Kiểm thử: JUnit; kiểm thử tích hợp chạy trên MySQL thật, không dùng H2.

## 2. Logic nghiệp vụ

- Giữ ghế 5 phút do máy chủ quyết định; tải lại trang, đổi combo, đổi voucher hay thanh toán đều không kéo dài thời gian giữ.
- Mỗi người một lượt giữ ghế, tối đa 8 ghế; ghế đôi bán theo cặp.
- Chỉ cho giữ ghế khi còn ít nhất 5 phút trước giờ đóng bán (15 phút trước giờ chiếu).
- Tiền tính bằng VND nguyên, chỉ tính ở backend; đơn lưu ảnh chụp giá, tên và giảm giá.
- Thanh toán xử lý lặp lại an toàn (idempotent), kiểm tra chữ ký, merchant, mã đơn, số tiền. Thanh toán thành công đến sau khi hết hạn giữ ghế thì ghi nhận để đối soát, không phát vé.
- Check-in: mỗi mã QR dùng một lần, trong khoảng 60 phút trước đến 30 phút sau giờ chiếu, chỉ tại rạp của nhân viên.
- Thời gian nghiệp vụ theo múi giờ Asia/Ho_Chi_Minh, tầng DB/JDBC dùng UTC.
- Đổi vai trò hoặc khóa tài khoản làm các phiên đăng nhập cũ mất hiệu lực (`authVersion`).

## 3. Ngôn ngữ JavaScript

- JavaScript thuần (không framework, không Node ở runtime), nằm trong `static/js`.
- Dùng cho: sơ đồ ghế và giữ ghế, chọn combo/voucher, trạng thái thanh toán, quét QR check-in, thông báo, màn xếp suất chiếu.
- Máy chủ vẫn là nơi kiểm tra cuối cùng; JS chỉ hỗ trợ hiển thị và thao tác.
- Màn xếp suất chiếu: bảng lịch cả tuần theo phòng, hộp thêm suất, chế độ theo giờ có kéo thả, nhập/xuất Excel.

## 4. Cơ sở dữ liệu: MySQL

- Quản lý bằng Flyway (`db/migration`), không sửa migration đã áp dụng.
- Ràng buộc duy nhất: tồn kho ghế theo (suất chiếu, ghế), mã QR, giao dịch, sổ điểm, lượt dùng voucher.
- Giao dịch nguyên tử (tất cả hoặc không gì cả) ở mức READ COMMITTED; khóa bi quan theo thứ tự cố định để tránh tranh chấp.
- Các tranh chấp (ghế, voucher, check-in) được kiểm chứng trên MySQL thật.

## 5. Phân tích nghiệp vụ

### 5.1. Nghiệp vụ của Admin

- Quản lý phim: thêm, sửa, ẩn; trạng thái Sắp chiếu / Đang chiếu tự suy ra từ ngày phát hành.
- Quản lý rạp, phòng chiếu, sơ đồ ghế, giá theo phòng.
- Quản lý suất chiếu: xếp lịch theo tuần cho từng phòng, thêm hàng loạt, nhập/xuất Excel, kiểm tra trùng giờ và thời gian dọn phòng.
- Quản lý combo, voucher, điểm thưởng.
- Quản lý người dùng và phân quyền (Customer, Admin, Staff), khóa tài khoản.
- Xem đơn hàng, đối soát thanh toán, báo cáo doanh thu và xuất CSV, nhật ký thao tác (audit).

### 5.2. Nghiệp vụ của Staff

- Check-in vé bằng quét QR hoặc nhập mã thủ công.
- Chỉ xử lý vé thuộc rạp được phân công; mỗi vé chỉ vào một lần.
- Xem danh sách vé, suất chiếu trong ngày tại rạp của mình.
- Nhân viên chưa được phân rạp thấy thông báo chờ phân công.

### 5.3. Nghiệp vụ của Khách hàng

- Xem danh sách phim, chi tiết phim, lịch chiếu theo rạp và ngày.
- Đăng ký, đăng nhập, quên và đặt lại mật khẩu.
- Chọn ghế (giữ 5 phút), thêm combo, áp voucher, dùng điểm.
- Thanh toán qua MoMo; nhận vé có mã QR và vé PDF.
- Xem lịch sử đơn, nhận email và thông báo nhắc suất chiếu.
