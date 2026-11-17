---
name: cineerd
description: Skill vẽ ERD (mô hình thực thể quan hệ) cho CinePrime ra draw.io (.drawio) kèm file Word (.docx) giải thích từng bảng, đúng phong cách ảnh mẫu người dùng gửi. Dùng khi người dùng nói "vẽ ERD", "sơ đồ thực thể", "thiết kế CSDL", "mô tả bảng", "file word ERD", hoặc gọi /cineerd [phạm vi].
argument-hint: "[toàn bộ | <nhóm bảng: phim | rạp | đặt vé | người dùng | nội dung>] [sửa <yêu cầu>]"
---

# /cineerd — ERD draw.io + Word giải thích

Đối số: `$ARGUMENTS`. Trống = ERD **toàn bộ** cơ sở dữ liệu.

Phong cách lấy từ ảnh mẫu người dùng gửi ngày 13/11/2026 (ERD đồ án WellFitness + các bảng "Thực thể" trong Word). Người dùng yêu cầu **giống hệt** ảnh mẫu, sơ đồ **không rối**, **không có quan hệ nhiều–nhiều**, đủ bảng và cột để người đọc hiểu hệ thống thực tế. Mỗi lần vẽ ERD **luôn** giao kèm file Word.

## 0. Công cụ (đã kiểm chứng 13/11/2026)

- [tao-erd.ps1](tao-erd.ps1) sinh **cả hai** file từ một JSON: `.drawio` (bảng, cột, quan hệ chân quạ) và `.docx` (bảng mô tả thực thể, tạo bằng Microsoft Word qua COM, chú thích "Hình/Bảng" là trường SEQ nên tự đánh số lại khi dán vào báo cáo).
- [mau-erd.json](mau-erd.json) là JSON mẫu 4 bảng (cinemas, rooms, seats, users). Đã sinh ra và render: đúng phong cách ảnh mẫu.
- **Không viết XML/Word bằng tay.** Chỉ soạn JSON rồi chạy script. Sửa ERD = sửa JSON rồi chạy lại.
- Script phải giữ **UTF-8 có BOM** (PowerShell 5.1). Sau khi sửa script, chạy lại lệnh ghi BOM ở mục 6.
- Script dùng Word đang cài: nếu Word của người dùng đang mở thì chỉ đóng file của script, **không Quit**.

## 1. Đầu ra (người dùng yêu cầu 13/11/2026)

- Thư mục đích `D:\0_JAV102\CinePrime_Project\Sơ đồ\ERD\` chỉ chứa **ảnh** `ERD01-toan-bo.png` (có nhúng XML draw.io để sửa tiếp) và file Word `ERD01-toan-bo.docx` đã chèn sẵn ảnh đó. **File Word luôn lưu kèm ngay cạnh ảnh trong thư mục này** (script mặc định ghi vào đây). Không lưu Word ở nơi khác để người dùng khỏi phải tìm. ERD theo nhóm: `ERD02-<nhom>`… Vẽ lại thì ghi đè đúng tên cũ.
- **Không tạo file `.py` hay script mới.** Chỉ dùng `tao-erd.ps1` có sẵn trong skill, các lệnh khác chạy inline. JSON, `.drawio` và `.pdf` soát đặt trong **thư mục tạm của session**. Giao xong phải xóa.
- Cách xuất ảnh, cách lấy XML từ ảnh để sửa, cách kiểm tra dọn dẹp: [../cinediagram/xuat-anh.md](../cinediagram/xuat-anh.md).

## 2. Hình thức ERD (bắt buộc — script đã cài sẵn, không đổi)

| Thành phần | Quy định (như ảnh mẫu) |
|---|---|
| Bảng | Khung đen nền trắng; tiêu đề tên bảng **đậm, căn giữa**, có đường kẻ dưới tiêu đề. |
| Cột khóa | Cột trái hẹp ghi `PK` / `FK` **đậm**; cột khóa xếp trên cùng: PK → các FK, tên cột khóa **đậm**. Đường kẻ ngang ngăn nhóm khóa với thuộc tính thường. |
| Thuộc tính | Chữ thường, cỡ 11, căn trái; đường kẻ dọc ngăn cột khóa chạy suốt chiều cao bảng. |
| Quan hệ | Đường đen, vuông góc, **bo góc**; gắn vào **đúng hàng** PK bên cha và hàng FK bên con, ra vào ở cạnh trái/phải bảng. |
| Ký hiệu | Chân quạ (Crow's foot). Bên cha: `‖` (ERmandOne) nếu FK NOT NULL; `|○` (ERzeroToOne) nếu FK NULL. Bên con: chân quạ (ERmany) cho 1–N; `|○` cho 1–1 (FK có UNIQUE). |
| Màu | Chỉ đen/trắng, Helvetica. Không tô màu nhóm, không ghi chú, không nhãn trên đường. |

**Không có quan hệ nhiều–nhiều**: mọi đường nối PK → FK (1–N hoặc 1–1). Quan hệ N–N trong nghiệp vụ luôn đi qua bảng trung gian có thật trong CSDL (`movie_genres`, `credits`, `favorites`, `reviews`, `subscriptions`, `order_seats`, `order_products`, `voucher_uses`, `inventory`, `tickets`…). Không tự thêm bảng không có trong migration (ví dụ không vẽ bảng Role — vai trò là cột `users.role`).

## 3. Nguồn dữ liệu — luôn theo CSDL thật

1. Đọc **toàn bộ** `src/main/resources/db/migration/V*.sql` theo thứ tự (V1 tạo bảng, các V sau ALTER/thêm bảng). Hiện có 33 bảng: audit_logs, banners, cinemas, contents, credits, favorites, genres, inventory, mail_jobs, movie_genres, movies, notifications, order_products, order_seats, orders, payments, people, point_ledger, prices, products, reset_tokens, reviews, rooms, seats, settings, shifts, showtime_batch_results, showtimes, subscriptions, tickets, users, voucher_uses, vouchers. Bỏ `flyway_schema_history`.
2. Cột: tên, kiểu, NULL/NOT NULL, PK, FK (`FOREIGN KEY … REFERENCES`), UNIQUE (một cột → "Duy nhất"; UNIQUE nhiều cột → ghi vào Mô tả, ví dụ "duy nhất cùng room_id"), CHECK (ghi ngắn vào Mô tả, ví dụ "1–5 sao").
3. Cột `*_id`/`*_by` **không có** khóa ngoại trong migration: không vẽ đường, không ghi "Khóa ngoại"; liệt kê cho người dùng khi giao.
4. Mô tả cột/bảng bằng tiếng Việt theo nghiệp vụ trong Plan.md §2/§5 và `Domain.java` (trạng thái đơn PENDING_PAYMENT/PAID/EXPIRED/CANCELLED; payment CREATED/PENDING/SUCCEEDED/FAILED; vé VALID/USED/VOID; tiền VND số nguyên).

## 4. JSON đặc tả (xem [mau-erd.json](mau-erd.json))

```json
{ "ten": "ERD CinePrime", "tenFile": "ERD01-toan-bo",
  "word": { "chuong": "2.3", "tieuDe": "Thiết kế cơ sở dữ liệu", "tenHinh": "Mô hình thực thể quan hệ ERD",
            "anhErd": "ERD01-toan-bo.png", "gioiThieu": ["- Đoạn giới thiệu …"] },
  "bang": [ { "id": "rooms", "x": 300, "y": 40, "rong": 180, "moTa": "dùng để lưu trữ …",
              "cot": [ ["id","bigint","NOT NULL","PK","Mã phòng chiếu"],
                       ["cinema_id","bigint","NOT NULL","FK","Mã rạp"],
                       ["name","varchar(255)","NOT NULL","","Tên phòng", true] ] } ],
  "quanHe": [ { "cha": "cinemas.id", "con": "rooms.cinema_id", "kieu": "1-N", "diem": [[260,98],[260,120]] } ] }
```

- `cot`: `[tên, kiểu, "NOT NULL"|"NULL", "PK"|"FK"|"", mô tả, (tùy chọn) true = UNIQUE]`. Kiểu viết thường như ảnh mẫu: BIGINT→`bigint`, INT→`int`, VARCHAR(n)→`varchar(n)`, TEXT→`text`, BOOLEAN→`boolean`, DATE→`date`, DATETIME(6)→`datetime`, DECIMAL(p,s)→`decimal(p,s)`. Thứ tự cột trong JSON theo migration; script tự đưa PK, FK lên đầu.
- `x`,`y`,`rong`: vị trí/chiều rộng bảng (px). Chiều cao tự tính: 30 + 22 × số cột.
- `quanHe.kieu`: `1-N` hoặc `1-1`. `diem` (tùy chọn): điểm gấp để dẫn đường theo kênh trống, tránh cắt bảng.
- `tenHienThi` (tùy chọn): tên hiển thị khác `id`. Mặc định dùng đúng tên bảng trong CSDL.
- Script báo lỗi nếu quan hệ trỏ tới bảng/cột không có.

## 5. Bố cục chống rối (ERD toàn bộ)

- Lưới cột: `x = 40 + k × 260` (bảng rộng 180–200, kênh dẫn đường rộng ≥ 60–80px giữa hai cột); bảng cách dọc ≥ 40px.
- Bảng trung tâm (nhiều quan hệ) đặt giữa: `users`, `orders`, `showtimes`, `movies`. Bảng con đặt **sát cạnh** bảng cha, hàng FK quay về phía cha.
- Vùng gợi ý (trái → phải, trên → dưới):
  - **Phim** (trên trái): genres, movie_genres, movies, credits, people, banners.
  - **Rạp & lịch** (trên phải): cinemas, rooms, seats, prices, showtimes, showtime_batch_results.
  - **Đặt vé & thanh toán** (giữa): inventory, orders, order_seats, order_products, products, vouchers, voucher_uses, payments, tickets, shifts.
  - **Người dùng & tương tác** (dưới trái): users, reset_tokens, favorites, reviews, subscriptions, notifications, point_ledger, mail_jobs, audit_logs.
  - **Nội dung độc lập** (dưới phải): contents, settings (không quan hệ thì xếp gọn ở góc).
- Đường dài: thêm `diem` cho chạy trong kênh giữa các cột; không cắt ngang bảng; hạn chế hai đường chồng nhau trên đoạn dài; nhiều đường từ một PK được phép chung điểm xuất phát (như ảnh mẫu).
- Quá dày khi xuất A4: vẫn giao ERD toàn bộ, rồi đề xuất thêm ERD theo nhóm (`/cineerd đặt vé`…). ERD theo nhóm vẽ bảng ngoài nhóm chỉ khi có quan hệ trực tiếp.

## 6. Quy trình

1. Đọc migration (mục 3) → soạn JSON trong **thư mục tạm của session** (`<tạm>\ERD01-toan-bo.json`), đặt `"anhErd": "D:\\0_JAV102\\CinePrime_Project\\Sơ đồ\\ERD\\ERD01-toan-bo.png"`. Toàn bộ 33 bảng: viết JSON theo từng vùng để dễ đặt tọa độ.
2. Sinh `.drawio` tạm: `powershell -NoProfile -ExecutionPolicy Bypass -File D:\0_JAV102\CinePrime_Project\.claude\skills\cineerd\tao-erd.ps1 -Spec <tạm>\ERD01-toan-bo.json -ChiDrawio`
3. Xuất ảnh: chạy khối lệnh inline ở [xuat-anh.md](../cinediagram/xuat-anh.md) §1 với `$in=<tạm>\ERD01-toan-bo.drawio`, `$out=…\Sơ đồ\ERD\ERD01-toan-bo.png`. Mở ảnh bằng Read để soát (ERD lớn thì soát kỹ từng vùng). Sai bố cục → sửa `x/y/diem` trong JSON, làm lại bước 2–3.
4. Sinh Word có ảnh: `… tao-erd.ps1 -Spec <tạm>\ERD01-toan-bo.json -ChiWord -Pdf`. Word tự lưu vào `Sơ đồ\ERD\` cạnh ảnh; `.pdf` soát nằm ở thư mục tạm. Đọc `.pdf` bằng Read để soát.
5. Dọn: xóa JSON, `.drawio` và `.pdf` tạm; chạy kiểm tra ở xuat-anh.md §3. Thư mục `Sơ đồ\ERD\` chỉ còn `.png` và `.docx`.
6. Giao: đường dẫn ảnh và Word, danh sách cột `*_id` không có FK (mục 3.3).
- Sửa ERD đã có: dựng lại JSON từ migration (hoặc lấy XML từ ảnh theo xuat-anh.md §2 để tham khảo tọa độ), rồi chạy lại bước 2–5.
- Sau khi sửa script: `$f='…\tao-erd.ps1'; [IO.File]::WriteAllText($f,[IO.File]::ReadAllText($f,[Text.Encoding]::UTF8),(New-Object Text.UTF8Encoding $true))`.

## 7. Định dạng Word (script đã cài — giống ảnh mẫu)

- A4, lề trên/dưới 2 cm, trái 3 cm, phải 2 cm; Times New Roman 13.
- `2.3.<tab>Thiết kế cơ sở dữ liệu` (đậm) → ảnh ERD căn giữa → "Hình N: Mô hình thực thể quan hệ ERD" căn giữa → đoạn giới thiệu (cờ `active`/`enabled` thay cho xóa cứng, như đoạn "Cờ xóa" của mẫu nhưng theo CSDL thật).
- Mỗi bảng: `2.3.n.<tab>Thực thể <tên>` (đậm) → dòng `- <tên>: dùng để lưu trữ …` → bảng 5 cột **Thuộc tính | Kiểu dữ liệu | Bắt buộc | Ràng buộc | Mô tả**, hàng tiêu đề **nền xám** (#D9D9D9), kẻ khung đủ, cỡ 11; **hàng PK in đậm cả hàng**; Ràng buộc: `Khóa chính`, `Khóa ngoại` (hoặc `Khóa ngoại 1`, `Khóa ngoại 2` khi bảng có nhiều FK), `Duy nhất`; → chú thích "Bảng N: Thực thể <tên>" căn giữa dưới bảng, không tách khỏi bảng. Hàng tiêu đề lặp lại khi bảng sang trang.

### Bảng soát

- [ ] Đủ mọi bảng/cột theo migration hiện tại; kiểu, NULL, PK/FK, UNIQUE đúng.
- [ ] Không quan hệ N–N; mỗi FK trong migration có đúng một đường; ký hiệu hai đầu đúng mục 2.
- [ ] Không đường cắt ngang bảng, không bảng chồng nhau, nhóm bảng theo vùng, đường gọn.
- [ ] Word: thứ tự mục và số Bảng khớp thứ tự bảng trong JSON; mô tả tiếng Việt có dấu, không bỏ trống.
- [ ] Ảnh xuất đủ khung, nét; Word có ảnh ERD và PDF soát đúng.
- [ ] `Sơ đồ\ERD\` chỉ có `.png` và `.docx`; không còn JSON, `.drawio`, `.pdf`, `.py` hay script tạm nào.
