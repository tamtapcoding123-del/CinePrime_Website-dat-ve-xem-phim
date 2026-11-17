# Cú pháp file nội dung cho tao-baocao.ps1

File `.txt` UTF-8, mỗi dòng một khối. Dòng trống và dòng bắt đầu `//` bị bỏ qua. Đường dẫn tương đối tính từ thư mục chứa file nội dung.

## Cấu hình bìa (đầu file)

| Dòng | Ý nghĩa |
|---|---|
| `@loaiBaoCao: BÁO CÁO DỰ ÁN` | Chữ lớn 24 đậm giữa bìa (mẫu: "ĐỒ ÁN TỐT NGHIỆP") |
| `@nganh:` · `@chuyenNganh:` · `@monHoc:` | Dòng "Ngành: **…**" cỡ 16; bỏ dòng nào thì bìa không in dòng đó |
| `@tieuDe:` | Tên đề tài, 24 đậm, sau "ĐỀ TÀI:" |
| `@gvhd:` · `@sinhVien: A; B; C` | Khối GV/SV: nhãn nghiêng căn phải, tên đậm; nhiều SV thì mỗi tên một dòng "-  Tên" |
| `@noiNgay: Đà Nẵng, 11/2026` | Dòng cuối bìa, 14 đậm |
| `@to1:` `@to2:` `@to3:` | Ba dòng đơn vị đầu bìa (mặc định FPT / Cao đẳng FPT Polytechnic / Bộ môn CNTT) |
| `@maBieuMau:` | Chữ trái chân trang (mặc định `07.01.16- BM/FPL/HDCV/FE`) |
| `@logo:` | Ảnh logo bìa (mặc định `logo-fpt-education.png` của skill) |

## Khối trang

| Dòng | Kết quả |
|---|---|
| `::bia` | Trang bìa có khung viền đôi, không chân trang, không số trang |
| `#!= TIÊU ĐỀ` | Trang mới, tiêu đề 14 đậm **căn giữa**, không vào mục lục (LỜI CẢM ƠN, CAM KẾT) |
| `#! TIÊU ĐỀ` | Trang mới, tiêu đề 14 đậm căn trái, không vào mục lục (DANH SÁCH THUẬT NGỮ, PHỤ LỤC) |
| `::muc-luc` · `::muc-luc-hinh` · `::muc-luc-bang` | Trang MỤC LỤC (Heading 1–3) / MỤC LỤC HÌNH ẢNH / MỤC LỤC BẢNG BIỂU, tự cập nhật số trang |
| `::ngat-trang` · `::trong` | Ngắt trang · một dòng trống |
| `::co 14` … `::co 12` | Đổi cỡ chữ đoạn văn cho các dòng sau (trang cảm ơn/cam kết dùng 14) |

## Tiêu đề (vào mục lục tới cấp `###`)

| Dòng | Kiểu (đo từ mẫu) |
|---|---|
| `# CHƯƠNG I: CƠ SỞ LÝ THUYẾT` | Heading 1: sang trang mới, 14 đậm, sát lề |
| `## 1. MÔ HÌNH CÔNG NGHỆ` | Heading 2: 12 đậm, cách trên 1 dòng; số ở 0,18 cm, chữ ở 1,25 cm (mẫu viết HOA) |
| `### 2.1. Mục đích của sản phẩm` | Heading 3: 12 đậm; số ở −0,14 cm, chữ ở 1,25 cm |
| `#### 2.3.1. Thực thể Role` | Heading 4: 12 đậm; số ở −0,45 cm (không vào mục lục) |

Số mục **gõ tay** đúng như mẫu (`1.`, `2.1.`, `3.1.1.`); script tách số và chữ bằng tab.

## Đoạn văn và đầu dòng

| Dòng | Kết quả |
|---|---|
| `Văn bản thường` | Đoạn căn đều, thụt 1,25 cm (thẳng chữ tiêu đề mục) |
| `> Văn bản` | Đoạn căn đều sát lề (lời cảm ơn, "Đặc tả:", "B1: …") |
| `^ Văn bản` | Đoạn căn giữa |
| `>> Văn bản` | Khối ký tên: căn giữa trong nửa phải trang |
| `- Văn bản` | Gạch đầu dòng cấp 1: "-" ở 0,5 cm, chữ 1,25 cm |
| `+ Văn bản` | Cấp 2: "+" ở 1,25 cm, chữ 2,0 cm |
| `* Văn bản` | Cấp 3: "•" ở 2,0 cm, chữ 2,75 cm |
| `% 1. \| Văn bản` | Danh sách đánh số (trang Cam kết): số 0,63 cm, chữ 1,27 cm |
| `**đậm**` · `__nghiêng__` | Định dạng trong dòng (lồng được: `**__đậm nghiêng__**`) |

Dòng nằm ngay trước `[hinh]`/`[bang]`/`[ma]` tự "giữ với đoạn sau" để không bị bỏ lại cuối trang.

## Hình, bảng, mã, chèn file

```
[hinh] đường\dẫn\anh.png | Chú thích hình | 14
```
Ảnh căn giữa, rộng tối đa 15,9 cm (hoặc số cm ở cột 3), cao tối đa 21 cm; dưới ảnh "Hình N: Chú thích" (trường SEQ, tự đánh số, vào MỤC LỤC HÌNH ẢNH). Thiếu file → chữ đỏ `[THIẾU ẢNH: …]` và cảnh báo.

```
[bang] Chú thích bảng || 1.2c,4,2.5,2.5,5
|# Tiêu đề 1 | Tiêu đề 2 | …        ← hàng tiêu đề nền xám #D9D9D9, đậm, lặp lại khi sang trang
|^ Tiêu đề 1 | …                    ← hàng tiêu đề đậm, nền trắng (bảng thuật ngữ, kiểm thử)
|= Nhóm hạng mục                     ← một ô trải hết chiều ngang, đậm
|~ Giao diện | Trang chủ             ← ô đầu nền xám (nhãn bảng đặc tả)
|! id | bigint | …                   ← cả hàng đậm (khóa chính)
| a | b<br>dòng 2 | …                ← hàng thường; <br> xuống dòng trong ô; \| là ký tự | thật
```
- Sau `||` là độ rộng tương đối các cột (tự co về 15,9 cm); thêm `c` để căn giữa cột (STT, Pass/Fail). Bỏ `||` = chia đều.
- Hàng có ít ô hơn số cột: ô cuối tự trải hết phần còn lại (dùng cho bảng đặc tả 2 ô trong bảng 3 cột).
- Chú thích để trống (`[bang] || …`) = bảng không có "Bảng N" (bảng thuật ngữ, đặc tả, kiểm thử như mẫu).

```
[ma]
mã nguồn giữ nguyên thụt lề
[/ma]
```
Khối mã nền tối #1E1E1E chữ #D4D4D4 (Consolas 9) như ảnh chụp VS Code trong mẫu.

```
[chen] D:\…\ERD01-toan-bo.docx | Mô hình thực thể quan hệ - ERD
```
Chèn nguyên nội dung một file Word khác (dùng cho mục 2.3 ERD do `/cineerd` sinh); tiêu đề Heading 2 của file chèn được hạ thành Heading 3 và đổi tên theo phần sau dấu `|`, Heading 3 thành Heading 4 (không vào mục lục). Style của báo cáo được áp lại; số Hình/Bảng nối tiếp.
