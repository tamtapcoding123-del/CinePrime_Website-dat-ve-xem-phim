<#
  Sinh file Word báo cáo đồ án CinePrime theo đúng trình bày của bản mẫu Mau-Bao-Cao-JAV102.pdf
  từ một file nội dung dạng văn bản (cú pháp: xem cu-phap.md cùng thư mục).
  Dùng:  powershell -NoProfile -ExecutionPolicy Bypass -File tao-baocao.ps1 -NoiDung <bao-cao.txt> -Ra <BaoCao.docx> [-Pdf]
  Ảnh/đường dẫn tương đối được tính từ thư mục chứa file nội dung.
#>
param([Parameter(Mandatory)][string]$NoiDung,[Parameter(Mandatory)][string]$Ra,[switch]$Pdf)
$ErrorActionPreference='Stop'
$NoiDung=(Resolve-Path -LiteralPath $NoiDung).Path
$baseDir=Split-Path $NoiDung -Parent
$skillDir=Split-Path $MyInvocation.MyCommand.Path -Parent
$lines=[IO.File]::ReadAllLines($NoiDung,[Text.Encoding]::UTF8)
function Abs($p){ if([IO.Path]::IsPathRooted($p)){$p}else{[IO.Path]::GetFullPath((Join-Path $baseDir $p))} }

# ---------- cấu hình (dòng "@khoa: giá trị" ở đầu file) ----------
$cfg=@{ maBieuMau='07.01.16- BM/FPL/HDCV/FE'; logo=(Join-Path $skillDir 'logo-fpt-education.png')
        to1='TỔ CHỨC GIÁO DỤC FPT'; to2='TRƯỜNG CAO ĐẲNG FPT POLYTECHNIC'; to3='BỘ MÔN CÔNG NGHỆ THÔNG TIN' }
foreach($l in $lines){ if($l -match '^@(\w+):\s*(.*)$'){ $cfg[$Matches[1]]=$Matches[2].Trim() } }
if($cfg.logo -and -not [IO.Path]::IsPathRooted($cfg.logo)){ $cfg.logo=Abs $cfg.logo }

# ---------- kích thước (đo từ PDF mẫu: A4, lề 2,54 cm, Times New Roman 12, giãn dòng 1,15) ----------
$cm=28.3465; $font='Times New Roman'; $textW=21.0-2*2.54
$GREY=14277081   # #D9D9D9
$CODEBG=1973790  # #1E1E1E
$CODEFG=13948116 # #D4D4D4
$bodySize=12

$word=New-Object -ComObject Word.Application
$shared=$word.Documents.Count -gt 0 -or $word.Visible
$doc=$null
try{
  $word.ScreenUpdating=$false
  $doc=$word.Documents.Add()
  $ps=$doc.PageSetup; $ps.PaperSize=7
  $ps.TopMargin=2.54*$cm; $ps.BottomMargin=2.54*$cm; $ps.LeftMargin=2.54*$cm; $ps.RightMargin=2.54*$cm
  $ps.FooterDistance=1.0*$cm; $ps.DifferentFirstPageHeaderFooter=-1

  # ---- style ----
  $n=$doc.Styles.Item(-1); $n.Font.Name=$font; $n.Font.Size=12; $n.Font.Color=0
  $n.ParagraphFormat.SpaceBefore=0; $n.ParagraphFormat.SpaceAfter=0; $n.ParagraphFormat.LineSpacingRule=5; $n.ParagraphFormat.LineSpacing=12*1.15
  $n.ParagraphFormat.Alignment=3
  $H=@{1=$doc.Styles.Item(-2);2=$doc.Styles.Item(-3);3=$doc.Styles.Item(-4);4=$doc.Styles.Item(-5)}
  foreach($k in 1..4){ $s=$H[$k]; $s.Font.Name=$font; $s.Font.Size=$(if($k -eq 1){14}else{12}); $s.Font.Bold=1; $s.Font.Italic=0; $s.Font.Color=0
    $pf=$s.ParagraphFormat; $pf.Alignment=0; $pf.LineSpacingRule=5; $pf.LineSpacing=12*1.15; $pf.KeepWithNext=-1; $pf.SpaceAfter=0
    $pf.SpaceBefore=$(switch($k){1{0}2{12}3{4}4{8}}); if($k -eq 1){ $pf.SpaceAfter=12; $pf.PageBreakBefore=-1 } }
  # vị trí số / chữ của heading (cm tính từ lề trái) — như mẫu: chữ thẳng cột 1,25 cm
  $HPOS=@{2=@(0.18,1.25);3=@(-0.14,1.25);4=@(-0.45,1.25)}
  foreach($k in 2..4){ $pf=$H[$k].ParagraphFormat; $pf.LeftIndent=$HPOS[$k][1]*$cm; $pf.FirstLineIndent=($HPOS[$k][0]-$HPOS[$k][1])*$cm }
  $pt=$doc.Styles.Add('CP Tieu de trang',1); $pt.BaseStyle='Normal'; $pt.Font.Size=14; $pt.Font.Bold=1
  $pt.ParagraphFormat.PageBreakBefore=-1; $pt.ParagraphFormat.SpaceAfter=12; $pt.ParagraphFormat.Alignment=0; $pt.ParagraphFormat.KeepWithNext=-1
  $cap=$doc.Styles.Item(-35); $cap.Font.Name=$font; $cap.Font.Size=12; $cap.Font.Bold=0; $cap.Font.Italic=0; $cap.Font.Color=0
  $cap.ParagraphFormat.Alignment=1; $cap.ParagraphFormat.SpaceBefore=4; $cap.ParagraphFormat.SpaceAfter=10; $cap.ParagraphFormat.LeftIndent=0; $cap.ParagraphFormat.FirstLineIndent=0
  foreach($i in 0..2){ $t=$doc.Styles.Item(-20-$i); $t.Font.Name=$font; $t.Font.Size=12; $t.Font.Bold=[int]($i -eq 0); $t.ParagraphFormat.SpaceAfter=0; $t.ParagraphFormat.LeftIndent=@(0,0.63,1.06)[$i]*$cm
    $t.ParagraphFormat.TabStops.ClearAll(); $null=$t.ParagraphFormat.TabStops.Add(@(1.0,1.4,2.2)[$i]*$cm); $null=$t.ParagraphFormat.TabStops.Add($textW*$cm,2,1) }
  $tf=$doc.Styles.Item(-36); $tf.Font.Name=$font; $tf.Font.Size=12; $tf.ParagraphFormat.SpaceAfter=0

  $sel=$word.Selection

  function Reset([int]$style=-1){ $sel.Style=$doc.Styles.Item($style); $f=$sel.Font; if($style -eq -1){ $f.Bold=0; $f.Italic=0; $f.Size=[single]$bodySize }; $f.Color=0; $f.Name=$font }
  function Keep(){ if($script:keep){ $sel.ParagraphFormat.KeepWithNext=-1 } }   # dòng dẫn ngay trước hình/bảng/mã: không tách trang
  function Rich([string]$t){  # **đậm**  __nghiêng__ (đảo so với định dạng nền của đoạn)
    $parts=[regex]::Split($t,'(\*\*|__)'); $b0=[int]($sel.Font.Bold -ne 0); $i0=[int]($sel.Font.Italic -ne 0); $b=$b0; $it=$i0
    foreach($p in $parts){ if($p -eq '**'){$b=1-$b;continue}; if($p -eq '__'){$it=1-$it;continue}; if($p -eq ''){continue}
      $sel.Font.Bold=$b; $sel.Font.Italic=$it; $sel.TypeText($p) }
    $sel.Font.Bold=$b0; $sel.Font.Italic=$i0 }
  function Para([string]$t,[double]$left=1.25,[double]$first=0,[int]$align=3){
    Reset; $pf=$sel.ParagraphFormat; $pf.LeftIndent=$left*$cm; $pf.FirstLineIndent=$first*$cm; $pf.Alignment=$align; Keep
    Rich $t; $sel.TypeParagraph() }
  function Hang([string]$mark,[string]$t,[double]$mPos,[double]$tPos){  # dòng có dấu đầu dòng, thụt treo
    Reset; $pf=$sel.ParagraphFormat; $pf.LeftIndent=$tPos*$cm; $pf.FirstLineIndent=($mPos-$tPos)*$cm; $pf.Alignment=3; Keep
    $pf.TabStops.ClearAll(); $null=$pf.TabStops.Add($tPos*$cm); $sel.TypeText("$mark`t"); Rich $t; $sel.TypeParagraph() }
  function Heading([int]$lv,[string]$t){
    Reset (-1-$lv)
    if($lv -ge 2 -and $t -match '^(\d+(\.\d+)*\.?)\s+(.*)$'){ $pf=$sel.ParagraphFormat; $pf.TabStops.ClearAll(); $null=$pf.TabStops.Add(1.25*$cm); $sel.TypeText("$($Matches[1])`t$($Matches[3])") }
    else { $sel.TypeText($t) }
    $sel.TypeParagraph() }
  function PageTitle([string]$t,[int]$align=0){ Reset; $sel.Style=$pt; $sel.Font.Size=14; $sel.Font.Bold=1; $sel.ParagraphFormat.Alignment=$align; $sel.TypeText($t); $sel.TypeParagraph() }
  function Caption([string]$label,[string]$t){
    Reset -35; $sel.TypeText("$label "); $null=$sel.Fields.Add($sel.Range,-1,"SEQ $label \* ARABIC",$false); $sel.TypeText(": $t"); $sel.TypeParagraph() }
  function Field([string]$code){ Reset; $sel.ParagraphFormat.LeftIndent=0; $null=$sel.Fields.Add($sel.Range,-1,$code,$false); $sel.TypeParagraph() }
  function Picture([string]$path,[string]$capt,[double]$wcm){
    $path=Abs $path
    Reset; $pf=$sel.ParagraphFormat; $pf.Alignment=1; $pf.LeftIndent=0; $pf.FirstLineIndent=0; $pf.KeepWithNext=-1; $pf.LineSpacingRule=0; $pf.SpaceBefore=6
    if(-not (Test-Path -LiteralPath $path)){ $sel.Font.Color=255; $sel.TypeText("[THIẾU ẢNH: $path]"); Write-Warning "Thiếu ảnh: $path" }
    else{ $s=$sel.InlineShapes.AddPicture($path,$false,$true); $s.LockAspectRatio=-1
      $maxW=$(if($wcm -gt 0){$wcm*$cm}else{$textW*$cm}); if($s.Width -gt $maxW -or $wcm -gt 0){ $s.Width=$maxW }
      if($s.Height -gt 21*$cm){ $s.Height=21*$cm } }
    $sel.TypeParagraph(); if($capt){ Caption 'Hình' $capt } }
  function Table([string]$capt,[string]$widths,[System.Collections.ArrayList]$rows,[string]$label='Bảng'){
    $ncol=($rows | ForEach-Object { if($_.kind -ne '='){ $_.cells.Count } else {1} } | Measure-Object -Maximum).Maximum
    Reset; $sel.ParagraphFormat.LeftIndent=0; $sel.ParagraphFormat.FirstLineIndent=0
    $t=$doc.Tables.Add($sel.Range,$rows.Count,$ncol); $t.Borders.Enable=1; $t.AllowAutoFit=$false
    $t.TopPadding=3; $t.BottomPadding=3; $t.LeftPadding=4; $t.RightPadding=4
    $r=$t.Range; $r.Font.Size=12; $r.Font.Name=$font; $pf=$r.ParagraphFormat; $pf.LeftIndent=0; $pf.FirstLineIndent=0; $pf.Alignment=0; $pf.LineSpacingRule=0; $pf.SpaceAfter=0; $pf.SpaceBefore=0
    $ws=@(); $al=@(); if($widths){ foreach($w in $widths.Split(',')){ $w=$w.Trim(); $al+=($w.EndsWith('c')); $ws+=[double]($w.TrimEnd('c')) } }
    if($ws.Count -ne $ncol){ $ws=@(1..$ncol | ForEach-Object { $textW/$ncol }); $al=@(1..$ncol | ForEach-Object { $false }) }
    $sum=($ws | Measure-Object -Sum).Sum; for($c=1;$c -le $ncol;$c++){ $t.Columns.Item($c).Width=$ws[$c-1]*$textW/$sum*$cm }
    for($i=0;$i -lt $rows.Count;$i++){ $row=$rows[$i]; $ri=$i+1
      if($row.kind -eq '='){ $t.Rows.Item($ri).Cells.Merge(); $cell=$t.Cell($ri,1); $cell.Range.Text=$row.cells[0]; $cell.Range.Font.Bold=1; continue }
      $nc=[Math]::Min($row.cells.Count,$ncol); if($nc -ge 1 -and $nc -lt $ncol){ $t.Cell($ri,$nc).Merge($t.Cell($ri,$ncol)) }   # hàng ít ô: ô cuối trải hết
      for($c=1;$c -le $nc;$c++){ $txt=$row.cells[$c-1]; $cell=$t.Cell($ri,$c); $cell.Range.Text=$txt
        if($al[$c-1]){ $cell.Range.ParagraphFormat.Alignment=1 }
        if($row.kind -in '#','^','!'){ $cell.Range.Font.Bold=1 }
        if($row.kind -eq '#' -or ($row.kind -eq '~' -and $c -eq 1)){ $cell.Shading.BackgroundPatternColor=$GREY } }
      if($row.kind -in '#','^'){ $t.Rows.Item($ri).HeadingFormat=-1 } }
    if($capt){ $t.Rows.Last.Range.ParagraphFormat.KeepWithNext=-1 }
    $sel.EndKey(6)|Out-Null
    if($capt){ Caption $label $capt } else { Reset; $sel.TypeParagraph() } }
  function Code([System.Collections.ArrayList]$code){
    Reset; $sel.ParagraphFormat.LeftIndent=0; $sel.ParagraphFormat.FirstLineIndent=0
    $t=$doc.Tables.Add($sel.Range,1,1); $t.Borders.Enable=0; $t.TopPadding=8; $t.BottomPadding=8; $t.LeftPadding=10; $t.RightPadding=10
    $t.Columns.Item(1).Width=$textW*$cm; $c=$t.Cell(1,1); $c.Shading.BackgroundPatternColor=$CODEBG
    $c.Range.Text=($code -join [char]11); $c.Range.Font.Name='Consolas'; $c.Range.Font.Size=9; $c.Range.Font.Color=$CODEFG
    $pf=$c.Range.ParagraphFormat; $pf.LeftIndent=0; $pf.FirstLineIndent=0; $pf.Alignment=0; $pf.LineSpacingRule=0; $pf.SpaceAfter=0
    $sel.EndKey(6)|Out-Null; Reset; $sel.TypeParagraph() }
  function Cover(){
    $pf=$sel.ParagraphFormat
    $sec=$doc.Sections.Item(1); $sec.Borders.EnableFirstPageInSection=$true; $sec.Borders.EnableOtherPagesInSection=$false
    foreach($b in -1,-2,-3,-4){ $bd=$sec.Borders.Item($b); $bd.LineStyle=13; try{$bd.LineWidth=24}catch{} }
    $sec.Borders.DistanceFrom=0; foreach($p in 'DistanceFromTop','DistanceFromBottom','DistanceFromLeft','DistanceFromRight'){ $sec.Borders.$p=12 }
    function C([string]$t,[int]$size,[int]$bold,[int]$before=0,[int]$after=0){ Reset; $p=$sel.ParagraphFormat; $p.Alignment=1; $p.LeftIndent=0; $p.FirstLineIndent=0; $p.SpaceBefore=$before; $p.SpaceAfter=$after; $p.LineSpacingRule=0; $sel.Font.Size=$size; $sel.Font.Bold=$bold; Rich $t; $sel.TypeParagraph() }
    C $cfg.to1 14 1 24; C $cfg.to2 14 1; C $cfg.to3 14 1; C '--------------------------------------------' 11 0 0 18
    if($cfg.logo -and (Test-Path -LiteralPath $cfg.logo)){ Reset; $sel.ParagraphFormat.Alignment=1; $sel.ParagraphFormat.LineSpacingRule=0; $s=$sel.InlineShapes.AddPicture($cfg.logo,$false,$true); $s.LockAspectRatio=-1; $s.Width=6.6*$cm; $sel.TypeParagraph() }
    C $cfg.loaiBaoCao 24 1 30 0
    if($cfg.nganh){ C "Ngành: **$($cfg.nganh)**" 16 0 }
    if($cfg.chuyenNganh){ C "Chuyên ngành: **$($cfg.chuyenNganh)**" 16 0 }
    if($cfg.monHoc){ C "Môn học: **$($cfg.monHoc)**" 16 0 }
    C 'ĐỀ TÀI:' 14 1 24; C $cfg.tieuDe 24 1 0 36
    $sv=@(); if($cfg.sinhVien){ $sv=$cfg.sinhVien.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ } }
    $rowsN=1+[Math]::Max(1,$sv.Count)
    Reset; $t=$doc.Tables.Add($sel.Range,$rowsN,2); $t.Borders.Enable=0; $t.Columns.Item(1).Width=7.0*$cm; $t.Columns.Item(2).Width=($textW-7.0)*$cm
    $t.Range.Font.Size=14; $t.Range.ParagraphFormat.SpaceAfter=6; $t.Range.ParagraphFormat.LineSpacingRule=0
    $t.Cell(1,1).Range.Text='Giảng viên hướng dẫn:'; $t.Cell(1,2).Range.Text=$cfg.gvhd
    for($i=0;$i -lt [Math]::Max(1,$sv.Count);$i++){ $r=$i+2; if($i -eq 0){ $t.Cell($r,1).Range.Text='Sinh viên thực hiện:' }; $t.Cell($r,2).Range.Text=$(if($sv.Count -gt 1){"-  $($sv[$i])"}elseif($sv.Count -eq 1){$sv[0]}else{''}) }
    for($r=1;$r -le $rowsN;$r++){ $a=$t.Cell($r,1).Range; $a.Font.Italic=1; $a.ParagraphFormat.Alignment=2; $b=$t.Cell($r,2).Range; $b.Font.Bold=1; $b.ParagraphFormat.Alignment=0 }
    $sel.EndKey(6)|Out-Null
    C $cfg.noiNgay 14 1 60 }

  # ---------- duyệt nội dung ----------
  $i=0; $N=$lines.Count
  while($i -lt $N){ $l=$lines[$i]; $i++
    $tl=$l.Trim()
    $j=$i; while($j -lt $N -and $lines[$j].Trim() -eq ''){ $j++ }; $script:keep=($j -lt $N -and $lines[$j].Trim().StartsWith('['))
    if($tl -eq '' -or $tl.StartsWith('//') -or $tl -match '^@\w+:'){ continue }
    if($tl -eq '[ma]'){ $code=New-Object System.Collections.ArrayList; while($i -lt $N -and $lines[$i].Trim() -ne '[/ma]'){ $null=$code.Add($lines[$i].TrimEnd()); $i++ }; $i++; Code $code; continue }
    if($tl -match '^\[bang\]\s*(.*)$'){
      $spec=$Matches[1]; $capt=$spec; $w=''; if($spec -match '^(.*?)\s*\|\|\s*(.*)$'){ $capt=$Matches[1]; $w=$Matches[2] }
      $rows=New-Object System.Collections.ArrayList
      while($i -lt $N -and $lines[$i].TrimStart().StartsWith('|')){ $raw=$lines[$i].Trim().Substring(1); $i++
        $kind=''; if($raw.Length -gt 0 -and '#^=~!'.Contains($raw[0])){ $kind=[string]$raw[0]; $raw=$raw.Substring(1) }
        $raw=$raw.Trim(); if($raw.EndsWith('|')){ $raw=$raw.Substring(0,$raw.Length-1) }
        $cells=@($raw.Replace('\|',[string][char]1).Split('|') | ForEach-Object { $_.Trim().Replace([string][char]1,'|').Replace('<br>',[string][char]11) })
        $null=$rows.Add(@{kind=$kind;cells=$cells}) }
      Table $capt.Trim() $w $rows; continue }
    if($tl -match '^\[hinh\]\s*(.*)$'){ $p=$Matches[1].Split('|'); $wcm=0; if($p.Count -gt 2 -and $p[2].Trim()){ $wcm=[double]$p[2].Trim() }; Picture $p[0].Trim() $(if($p.Count -gt 1){$p[1].Trim()}else{''}) $wcm; continue }
    if($tl -match '^\[chen\]\s*([^|]*?)\s*(\|\s*(.*))?$'){ $f=Abs $Matches[1].Trim(); $newTitle=$Matches[3]
      if(Test-Path -LiteralPath $f){ Reset; $s0=$sel.Start; $sel.InsertFile($f); $sel.EndKey(6)|Out-Null
        # hạ cấp tiêu đề của file chèn: Heading 2 → 3 (kèm đổi tên nếu có), Heading 3 → 4 (không vào mục lục)
        foreach($p in $doc.Range($s0,$sel.Start).Paragraphs){ $sn=$p.Style.NameLocal; $r=$p.Range
          if($sn -in 'Heading 2','Heading 3'){ $txt=$r.Text.TrimEnd([char]13); if($sn -eq 'Heading 2' -and $newTitle -and $txt -match '^(\d+(\.\d+)*\.?)\s+'){ $txt="$($Matches[1])`t$newTitle" }
            $lv=$(if($sn -eq 'Heading 2'){3}else{4}); $r.MoveEnd(1,-1)|Out-Null; $r.Text=$txt; $p.Style=$doc.Styles.Item(-1-$lv)
            $p.TabStops.ClearAll(); $null=$p.TabStops.Add(1.25*$cm) } } } else { Para "[THIẾU FILE: $f]"; Write-Warning "Thiếu file: $f" }; continue }
    if($tl -eq '::bia'){ Cover; continue }
    if($tl -eq '::muc-luc'){ PageTitle 'MỤC LỤC'; Field 'TOC \o "1-3" \h \z \u'; continue }
    if($tl -eq '::muc-luc-hinh'){ PageTitle 'MỤC LỤC HÌNH ẢNH'; Field 'TOC \h \z \c "Hình"'; continue }
    if($tl -eq '::muc-luc-bang'){ PageTitle 'MỤC LỤC BẢNG BIỂU'; Field 'TOC \h \z \c "Bảng"'; continue }
    if($tl -eq '::ngat-trang'){ $sel.InsertBreak(7); continue }
    if($tl -eq '::trong'){ Reset; $sel.TypeParagraph(); continue }
    if($tl -match '^::co\s+(\d+(\.\d+)?)$'){ $bodySize=[double]$Matches[1]; continue }
    if($tl -match '^#!=\s+(.*)$'){ PageTitle $Matches[1] 1; continue }
    if($tl -match '^#!\s+(.*)$'){ PageTitle $Matches[1] 0; continue }
    if($tl -match '^(#{1,4})\s+(.*)$'){ Heading $Matches[1].Length $Matches[2]; continue }
    if($tl -match '^-\s+(.*)$'){ Hang '-' $Matches[1] 0.5 1.25; continue }
    if($tl -match '^\+\s+(.*)$'){ Hang '+' $Matches[1] 1.25 2.0; continue }
    if($tl -match '^\*\s+(.*)$'){ Hang ([string][char]0x2022) $Matches[1] 2.0 2.75; continue }
    if($tl -match '^%\s+(\S+)\s*\|\s*(.*)$'){ Hang $Matches[1] $Matches[2] 0.63 1.27; continue }
    if($tl -match '^>>\s+(.*)$'){ Para $Matches[1] 8.0 0 1; continue }
    if($tl -match '^>\s+(.*)$'){ Para $Matches[1] 0 0 3; continue }
    if($tl -match '^\^\s+(.*)$'){ Para $Matches[1] 0 0 1; continue }
    Para $tl 1.25 0 3
  }

  # ---------- chân trang: mã biểu mẫu trái, số trang phải, kẻ trên; áp lại khổ giấy cho mọi section ----------
  $doc.PageSetup.OddAndEvenPagesHeaderFooter=0
  foreach($sec in $doc.Sections){ $p=$sec.PageSetup; $p.PaperSize=7; $p.TopMargin=2.54*$cm; $p.BottomMargin=2.54*$cm; $p.LeftMargin=2.54*$cm; $p.RightMargin=2.54*$cm; $p.FooterDistance=1.0*$cm }
  $doc.Sections.Item(1).PageSetup.DifferentFirstPageHeaderFooter=-1
  $ft=$doc.Sections.Item(1).Footers.Item(1)
  $ft.PageNumbers.RestartNumberingAtSection=$true; $ft.PageNumbers.StartingNumber=0
  $fr=$ft.Range; $fr.Text="$($cfg.maBieuMau)`t"; $fr.Font.Name=$font; $fr.Font.Size=11
  $fp=$fr.Paragraphs.Item(1); $fp.Alignment=0; $fp.LeftIndent=0; $fp.FirstLineIndent=0; $fp.SpaceBefore=4; $fp.TabStops.ClearAll(); $null=$fp.TabStops.Add($textW*$cm,2)
  $end=$ft.Range; $end.MoveEnd(1,-1)|Out-Null; $end.Collapse(0); $fld=$doc.Fields.Add($end,33); $fld.Result.Font.Name='Arial'; $fld.Result.Font.Size=10.5
  $bds=$ft.Range.Paragraphs.Item(1).Borders; $bd=$bds.Item(-1); $bd.LineStyle=1; $bd.LineWidth=6; $bd.Color=0; $bds.DistanceFromTop=3
  for($k=2;$k -le $doc.Sections.Count;$k++){ $s=$doc.Sections.Item($k); $s.PageSetup.DifferentFirstPageHeaderFooter=0; $s.Footers.Item(1).LinkToPrevious=$true; $s.Footers.Item(1).PageNumbers.RestartNumberingAtSection=$false }
  $doc.Fields.Update()|Out-Null
  foreach($t in $doc.TablesOfContents){ $t.Update() }
  foreach($t in $doc.TablesOfFigures){ $t.Update() }
  $out=[IO.Path]::GetFullPath($Ra); $dir=Split-Path $out -Parent; if(-not (Test-Path $dir)){ New-Item -ItemType Directory $dir|Out-Null }
  $doc.SaveAs2($out,16)
  if($Pdf){ # xuất PDF từ bản đã lưu (xuất ngay trong phiên dựng có thể thiếu đường kẻ chân trang)
    $doc.Close(0); $word.ScreenUpdating=$true; $doc=$word.Documents.Open($out,$false,$true); $doc.SaveAs2([IO.Path]::ChangeExtension($out,'.pdf'),17) }
  Write-Output "Đã tạo: $out ($($doc.ComputeStatistics(2)) trang)"
} finally {
  if($doc){ $doc.Close(0) }
  $word.ScreenUpdating=$true
  if(-not $shared){ $word.Quit() }
  [void][Runtime.InteropServices.Marshal]::ReleaseComObject($word)
}
