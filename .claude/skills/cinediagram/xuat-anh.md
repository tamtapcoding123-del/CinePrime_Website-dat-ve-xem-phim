# Xuất ảnh sơ đồ draw.io — dùng chung cho /cinediagram, /cineerd, /cineactivity

Kiểm chứng ngày 13/11/2026 trên máy này (Edge headless + trình xem draw.io): ảnh nét gấp đôi, đủ khung, tiếng Việt đúng. XML lấy ra từ ảnh khớp 100% bản gốc.

## Quy tắc đầu ra (người dùng yêu cầu 13/11/2026)

- **Đầu ra cuối cùng chỉ là ảnh PNG** có nhúng sẵn XML draw.io. Mở ảnh trong draw.io (kéo thả hoặc File > Open) là sửa tiếp được, đồng thời dán thẳng vào Word được. Ngoại lệ duy nhất: `/cineerd` giao kèm file Word giải thích, vì người dùng đã yêu cầu riêng.
- **Không tạo file `.py` hay script mới** (`.ps1`, `.js`, `.bat`, `.html`…), kể cả file tạm. Mọi lệnh chạy **inline** bằng công cụ PowerShell. Chỉ `/cineerd` dùng sẵn `tao-erd.ps1` có trong skill.
- File trung gian (`.drawio`, `.json`, `.pdf`, HTML tạm của Edge) chỉ nằm trong thư mục tạm của session hoặc `%TEMP%` và **phải xóa** sau khi xuất. Thư mục đích chỉ còn ảnh (và `.docx` của ERD).
- Thư mục đích (tạo nếu chưa có):
  - Use case, Sitemap → `D:\0_JAV102\CinePrime_Project\Sơ đồ\Usecase\` — tên `UC01-tong-quat.png`, `SM01-sitemap.png`…
  - ERD → `D:\0_JAV102\CinePrime_Project\Sơ đồ\ERD\` — `ERD01-toan-bo.png` (+ `ERD01-toan-bo.docx`)
  - Activity → `D:\0_JAV102\CinePrime_Project\Sơ đồ\Activity-Diagrams\` — `AD01-dang-ky-dang-nhap.drawio.png`…
  - Giữ đúng tên file đã có trong thư mục khi vẽ lại để **ghi đè**, không tạo bản trùng.

## 1. Xuất `.drawio` → `.png` có nhúng XML

Đặt `$in` (file `.drawio` tạm) và `$out` (ảnh đích), rồi chạy nguyên khối bằng công cụ PowerShell. Cần mạng để tải `viewer-static.min.js`. Dữ liệu sơ đồ nằm trong file HTML cục bộ, không gửi lên máy chủ. Edge chạy với profile tạm riêng nên không ảnh hưởng Edge của người dùng.

```powershell
$in='<tạm>\X.drawio'; $out='D:\0_JAV102\CinePrime_Project\Sơ đồ\<thư mục>\X.png'
$xmlText=[IO.File]::ReadAllText($in,[Text.Encoding]::UTF8); $x=[xml]$xmlText
$v=$x.SelectNodes('//mxCell[@vertex="1" and @parent="1"]/mxGeometry'); $x0=($v|%{[double]$_.x}|Measure -Min).Minimum; $y0=($v|%{[double]$_.y}|Measure -Min).Minimum
$x1=($v|%{[double]$_.x+[double]$_.width}|Measure -Max).Maximum; $y1=($v|%{[double]$_.y+[double]$_.height}|Measure -Max).Maximum
$B=20; $W=[int]($x1-$x0+2*$B+2); $H=[int]($y1-$y0+2*$B+2)
$tmp=Join-Path $env:TEMP ('cinedraw-'+[guid]::NewGuid()); New-Item -ItemType Directory $tmp | Out-Null; New-Item -ItemType Directory -Force (Split-Path $out) | Out-Null
try{
  $cfg=[ordered]@{xml=$xmlText;border=$B;resize=$false;nav=$false;highlight='none';lightbox=$false} | ConvertTo-Json -Compress
  [IO.File]::WriteAllText("$tmp\v.html","<!doctype html><html><head><meta charset=""utf-8""><style>html,body{margin:0;padding:0;background:#fff;overflow:hidden}</style></head><body><div class=""mxgraph"" data-mxgraph=""$([Net.WebUtility]::HtmlEncode($cfg))""></div><script src=""https://viewer.diagrams.net/js/viewer-static.min.js""></script></body></html>",(New-Object Text.UTF8Encoding $false))
  Start-Process "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe" -ArgumentList @('--headless=new','--disable-gpu','--hide-scrollbars','--no-first-run',"--user-data-dir=$tmp\p",'--force-device-scale-factor=2',"--window-size=$W,$H",'--virtual-time-budget=15000',"--screenshot=$tmp\s.png","file:///$($tmp -replace '\\','/')/v.html") -Wait -WindowStyle Hidden
  $png=[IO.File]::ReadAllBytes("$tmp\s.png")
  $sb=New-Object Text.StringBuilder; for($i=0;$i -lt $xmlText.Length;$i+=20000){ $null=$sb.Append([Uri]::EscapeDataString($xmlText.Substring($i,[Math]::Min(20000,$xmlText.Length-$i)))) }
  $data=[Text.Encoding]::ASCII.GetBytes('mxfile')+[byte[]](0)+[Text.Encoding]::ASCII.GetBytes($sb.ToString())
  $tbl=New-Object 'uint32[]' 256; for($n=0;$n -lt 256;$n++){ $c=[uint32]$n; for($k=0;$k -lt 8;$k++){ if($c -band 1){$c=[uint32](3988292384 -bxor ($c -shr 1))}else{$c=$c -shr 1} }; $tbl[$n]=$c }
  $typ=[Text.Encoding]::ASCII.GetBytes('tEXt'); $crc=[uint32]4294967295; foreach($bt in ($typ+$data)){ $crc=$tbl[($crc -bxor $bt) -band 255] -bxor ($crc -shr 8) }; $crc=$crc -bxor [uint32]4294967295
  $be={param([uint32]$u) $b=[BitConverter]::GetBytes($u); [Array]::Reverse($b); ,$b}
  $iend=$png.Length-12; [IO.File]::WriteAllBytes($out,[byte[]]($png[0..($iend-1)]+(& $be ([uint32]$data.Length))+$typ+$data+(& $be $crc)+$png[$iend..($png.Length-1)]))
  "OK $out ${W}x${H} (x2)"
} finally { Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue }
```

- **Không truyền khóa `toolbar`** vào cấu hình trình xem. Truyền `toolbar:''` làm sơ đồ lệch xuống khoảng 26px và bị cắt đáy.
- Kích thước tính từ các đỉnh `parent="1"`. Sơ đồ nào cũng phải có ít nhất một đỉnh cấp 1, ví dụ khung ngoài hoặc bảng ERD.
- Xuất xong: mở ảnh bằng Read để soát (thay cho bước render bằng link viewer). Sai thì sửa `.drawio` tạm rồi xuất lại.

## 2. Sửa sơ đồ đã có (chỉ còn ảnh)

Lấy lại XML từ ảnh ra file `.drawio` tạm, sửa bằng Edit, xuất lại đè lên ảnh cũ, rồi xóa file tạm:

```powershell
$png='D:\0_JAV102\CinePrime_Project\Sơ đồ\<thư mục>\X.png'; $in='<tạm>\X.drawio'
$b=[IO.File]::ReadAllBytes($png); $p=8; $xml=$null
while($p -lt $b.Length){ $len=[BitConverter]::ToInt32(($b[($p+3)..$p]),0); $t=[Text.Encoding]::ASCII.GetString($b,$p+4,4); if($t -eq 'tEXt'){ $d=[Text.Encoding]::ASCII.GetString($b,$p+8,$len); if($d.StartsWith("mxfile`0")){ $xml=[Uri]::UnescapeDataString($d.Substring(7)) } }; $p+=12+$len }
if(-not $xml){ throw 'Ảnh không có XML draw.io nhúng — phải vẽ lại.' }; [IO.File]::WriteAllText($in,$xml,(New-Object Text.UTF8Encoding $false))
```

## 3. Dọn dẹp cuối cùng (bắt buộc trước khi giao)

```powershell
Get-ChildItem -Recurse -File 'D:\0_JAV102\CinePrime_Project\Sơ đồ' | ? { $_.Extension -notin '.png','.docx' } | % FullName   # phải trống
```

Xóa mọi file `.drawio`/`.json`/`.pdf`/script tạm đã tạo trong session. Không để file nào ngoài ảnh (và `.docx` ERD) trong `Sơ đồ\`.
