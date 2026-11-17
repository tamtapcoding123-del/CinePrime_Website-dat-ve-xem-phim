<#
  Sinh ERD draw.io (.drawio) và file Word giải thích (.docx) từ một file JSON mô tả bảng.
  Dùng:  powershell -ExecutionPolicy Bypass -File tao-erd.ps1 -Spec <erd.json> [-ChiDrawio] [-ChiWord] [-Pdf]
  .drawio và .pdf (soát) ghi cạnh JSON (thư mục tạm); .docx luôn ghi vào D:\0_JAV102\CinePrime_Project\Sơ đồ\ERD (cùng chỗ với ảnh ERD), -OutDir chỉ để chạy thử. anhErd có thể là đường dẫn tuyệt đối.
  File này phải lưu UTF-8 có BOM (PowerShell 5.1 đọc file không BOM theo ANSI).
#>
param([Parameter(Mandatory)][string]$Spec,[string]$OutDir,[switch]$ChiDrawio,[switch]$ChiWord,[switch]$Pdf)
$ErrorActionPreference='Stop'
$specPath=(Resolve-Path $Spec).Path
$s=Get-Content -Raw -Encoding UTF8 $specPath | ConvertFrom-Json
$dir=Split-Path $specPath; $outDir=if($OutDir){$OutDir}else{'D:\0_JAV102\CinePrime_Project\Sơ đồ\ERD'}; $null=New-Item -ItemType Directory -Force $outDir; $name=if($s.tenFile){$s.tenFile}else{[IO.Path]::GetFileNameWithoutExtension($specPath)}

function Esc([string]$t){ [Security.SecurityElement]::Escape($t) }
function Cols($b){
  # Thứ tự: PK -> FK -> thuộc tính thường (giống ảnh mẫu)
  $all=@($b.cot | ForEach-Object { [pscustomobject]@{ten=$_[0];kieu=$_[1];batBuoc=$_[2];khoa=$_[3];moTa=$_[4];duyNhat=($_.Count -gt 5 -and $_[5])} })
  @($all|?{$_.khoa -eq 'PK'})+@($all|?{$_.khoa -eq 'FK'})+@($all|?{$_.khoa -notin 'PK','FK'})
}
$tables=@{}; foreach($b in $s.bang){ $tables[$b.id]=$b }
foreach($q in $s.quanHe){ foreach($end in $q.cha,$q.con){ $t,$c=$end.Split('.'); if(-not $tables[$t]){throw "Quan hệ trỏ tới bảng không có: $end"}; if(-not (Cols $tables[$t] | ?{$_.ten -eq $c})){throw "Quan hệ trỏ tới cột không có: $end"} } }

# ---------------- draw.io ----------------
if(-not $ChiWord){
  $HDR=30; $ROWH=22; $KEYW=34
  $TABLE='shape=table;startSize=30;container=1;collapsible=0;childLayout=tableLayout;fixedRows=1;rowLines=0;fontStyle=1;align=center;resizeLast=1;html=1;fillColor=#FFFFFF;strokeColor=#000000;fontColor=#000000;fontSize=11;fontFamily=Helvetica;'
  $ROW='shape=tableRow;horizontal=0;startSize=0;swimlaneHead=0;swimlaneBody=0;fillColor=none;collapsible=0;dropTarget=0;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;top=0;left=0;right=0;html=1;strokeColor=#000000;'
  $CELL='shape=partialRectangle;connectable=0;fillColor=none;top=0;left=0;bottom=0;overflow=hidden;whiteSpace=wrap;html=1;fontSize=11;fontFamily=Helvetica;fontColor=#000000;strokeColor=#000000;'
  $EDGE='edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;jettySize=auto;html=1;startFill=0;endFill=0;startSize=10;endSize=10;strokeColor=#000000;'
  $sb=New-Object Text.StringBuilder; $maxX=0; $maxY=0
  $null=$sb.Append('<mxfile host="app.diagrams.net"><diagram id="erd" name="'+(Esc $s.ten)+'"><mxGraphModel grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="PW" pageHeight="PH" math="0" shadow="0"><root><mxCell id="0"/><mxCell id="1" parent="0"/>')
  foreach($b in $s.bang){
    $cols=Cols $b; $w=if($b.rong){[int]$b.rong}else{180}; $th=$HDR+$ROWH*$cols.Count; $lastKey=($cols|?{$_.khoa -in 'PK','FK'}|Select -Last 1).ten
    $null=$sb.Append("<mxCell id=""$($b.id)"" value=""$(Esc $(if($b.tenHienThi){$b.tenHienThi}else{$b.id}))"" style=""$TABLE"" vertex=""1"" parent=""1""><mxGeometry x=""$($b.x)"" y=""$($b.y)"" width=""$w"" height=""$th"" as=""geometry""/></mxCell>")
    $y=$HDR
    foreach($c in $cols){
      $rid="$($b.id).$($c.ten)"; $isKey=$c.khoa -in 'PK','FK'; $bold=if($isKey){1}else{0}; $bottom=if($c.ten -eq $lastKey -and $cols.Count -gt 1 -and ($cols|?{$_.khoa -notin 'PK','FK'})){1}else{0}
      $null=$sb.Append("<mxCell id=""$rid"" value="""" style=""${ROW}bottom=$bottom;"" vertex=""1"" parent=""$($b.id)""><mxGeometry y=""$y"" width=""$w"" height=""$ROWH"" as=""geometry""/></mxCell>")
      $null=$sb.Append("<mxCell id=""$rid.k"" value=""$(if($isKey){$c.khoa})"" style=""${CELL}right=1;fontStyle=1;"" vertex=""1"" parent=""$rid""><mxGeometry width=""$KEYW"" height=""$ROWH"" as=""geometry""><mxRectangle width=""$KEYW"" height=""$ROWH"" as=""alternateBounds""/></mxGeometry></mxCell>")
      $null=$sb.Append("<mxCell id=""$rid.n"" value=""$(Esc $c.ten)"" style=""${CELL}right=0;align=left;spacingLeft=6;fontStyle=$bold;"" vertex=""1"" parent=""$rid""><mxGeometry x=""$KEYW"" width=""$($w-$KEYW)"" height=""$ROWH"" as=""geometry""><mxRectangle width=""$($w-$KEYW)"" height=""$ROWH"" as=""alternateBounds""/></mxGeometry></mxCell>")
      $y+=$ROWH
    }
    $maxX=[Math]::Max($maxX,[int]$b.x+$w); $maxY=[Math]::Max($maxY,[int]$b.y+$th)
  }
  $i=0
  foreach($q in $s.quanHe){
    $i++; $t,$c=$q.con.Split('.'); $child=Cols $tables[$t] | ?{$_.ten -eq $c}
    $start=if($child.batBuoc -eq 'NULL'){'ERzeroToOne'}else{'ERmandOne'}
    $end=if($q.kieu -eq '1-1'){'ERzeroToOne'}else{'ERmany'}
    $pts=''; if($q.diem){ $pts='<Array as="points">'+(($q.diem|%{"<mxPoint x=""$($_[0])"" y=""$($_[1])""/>"}) -join '')+'</Array>' }
    $null=$sb.Append("<mxCell id=""r$i"" value="""" style=""${EDGE}startArrow=$start;endArrow=$end;"" edge=""1"" parent=""1"" source=""$($q.cha)"" target=""$($q.con)""><mxGeometry relative=""1"" as=""geometry"">$pts</mxGeometry></mxCell>")
  }
  $null=$sb.Append('</root></mxGraphModel></diagram></mxfile>')
  $xml=$sb.ToString().Replace('pageWidth="PW"',"pageWidth=""$($maxX+60)""").Replace('pageHeight="PH"',"pageHeight=""$($maxY+60)""")
  $null=[xml]$xml
  [IO.File]::WriteAllText("$dir\$name.drawio",$xml,(New-Object Text.UTF8Encoding $false))
  "drawio: $dir\$name.drawio ($($s.bang.Count) bảng, $($s.quanHe.Count) quan hệ)"
}

# ---------------- Word ----------------
if(-not $ChiDrawio){
  $word=New-Object -ComObject Word.Application
  $sharedInstance=$word.Documents.Count -gt 0 -or $word.Visible   # Word của người dùng: chỉ đóng file của mình, không Quit
  $doc=$word.Documents.Add()
  try{
    $font='Times New Roman'; $cm=28.3465
    $ps=$doc.PageSetup; $ps.PaperSize=7; $ps.TopMargin=2*$cm; $ps.BottomMargin=2*$cm; $ps.LeftMargin=3*$cm; $ps.RightMargin=2*$cm
    $st=$doc.Styles.Item(-1); $st.Font.Name=$font; $st.Font.Size=13; $st.ParagraphFormat.SpaceAfter=6; $st.ParagraphFormat.SpaceBefore=0; $st.ParagraphFormat.LineSpacingRule=0
    foreach($lv in -3,-4){ $hs=$doc.Styles.Item($lv); $hs.Font.Name=$font; $hs.Font.Size=13; $hs.Font.Bold=1; $hs.Font.Italic=0; $hs.Font.Color=0; $hs.ParagraphFormat.SpaceBefore=6; $hs.ParagraphFormat.SpaceAfter=3; $hs.ParagraphFormat.KeepWithNext=-1 }
    $cap=$doc.Styles.Item(-35); $cap.Font.Name=$font; $cap.Font.Size=12; $cap.Font.Bold=0; $cap.Font.Italic=0; $cap.Font.Color=0; $cap.ParagraphFormat.Alignment=1; $cap.ParagraphFormat.SpaceBefore=3; $cap.ParagraphFormat.SpaceAfter=6
    $sel=$word.Selection
    function Para([string]$text,[int]$style=-1,[int]$align=0,[double]$indent=0,[int]$bold=0){
      $sel.Style=$doc.Styles.Item($style); $sel.ParagraphFormat.Alignment=$align; $sel.ParagraphFormat.LeftIndent=$indent*$cm; $sel.ParagraphFormat.FirstLineIndent=0; $sel.Font.Bold=$bold; $sel.TypeText($text); $sel.TypeParagraph()
    }
    function Caption([string]$label,[string]$title){
      $sel.Style=$doc.Styles.Item(-35); $sel.TypeText("$label "); $null=$sel.Fields.Add($sel.Range,-1,"SEQ $label \* ARABIC",$false); $sel.TypeText(": $title"); $sel.TypeParagraph()
    }
    $chuong=$s.word.chuong
    Para "$chuong.`t$($s.word.tieuDe)" -3 0 0 1
    $img=if($s.word.anhErd -and [IO.Path]::IsPathRooted($s.word.anhErd)){$s.word.anhErd}elseif($s.word.anhErd){Join-Path $dir $s.word.anhErd}
    if($img -and (Test-Path $img)){
      $sel.Style=$doc.Styles.Item(-1); $sel.ParagraphFormat.Alignment=1
      $pic=$sel.InlineShapes.AddPicture((Resolve-Path $img).Path); $maxW=16*$cm; if($pic.Width -gt $maxW){ $r=$maxW/$pic.Width; $pic.Width=$maxW; $pic.Height=$pic.Height*$r }
      $sel.TypeParagraph()
    } else { Para "[Chưa có ảnh ERD: xuất ảnh trước rồi chạy lại -ChiWord]" -1 1 0 0 }
    Caption 'Hình' $s.word.tenHinh
    foreach($p in $s.word.gioiThieu){ Para $p -1 3 0.5 }
    $n=0
    foreach($b in $s.bang){
      $n++; $cols=Cols $b; $ten=if($b.tenHienThi){$b.tenHienThi}else{$b.id}
      Para "$chuong.$n.`tThực thể $ten" -4 0 0 1
      Para "- ${ten}: $($b.moTa)" -1 3 0.75
      $fks=@($cols|?{$_.khoa -eq 'FK'}); $fkNo=0
      $tbl=$doc.Tables.Add($sel.Range,$cols.Count+1,5)
      $tbl.Borders.Enable=1; $tbl.Range.Font.Size=11; $tbl.Range.Font.Name=$font; $tbl.Range.ParagraphFormat.SpaceAfter=0; $tbl.Range.ParagraphFormat.Alignment=0; $tbl.Rows.AllowBreakAcrossPages=0; $tbl.TopPadding=3; $tbl.BottomPadding=3
      $widths=3.0,2.6,2.4,2.4,5.6; for($c=1;$c -le 5;$c++){ $tbl.Columns.Item($c).Width=$widths[$c-1]*$cm }
      $hdr='Thuộc tính','Kiểu dữ liệu','Bắt buộc','Ràng buộc','Mô tả'
      for($c=1;$c -le 5;$c++){ $cell=$tbl.Cell(1,$c); $cell.Range.Text=$hdr[$c-1]; $cell.Shading.BackgroundPatternColor=14277081 }
      $tbl.Rows.Item(1).HeadingFormat=-1
      $r=1
      foreach($c in $cols){
        $r++
        $rb=switch($c.khoa){ 'PK'{'Khóa chính'} 'FK'{ $fkNo++; if($fks.Count -gt 1){"Khóa ngoại $fkNo"}else{'Khóa ngoại'} } default{ if($c.duyNhat){'Duy nhất'}else{''} } }
        $vals=$c.ten,$c.kieu,$c.batBuoc,$rb,$c.moTa
        for($k=1;$k -le 5;$k++){ $tbl.Cell($r,$k).Range.Text=$vals[$k-1] }
        if($c.khoa -eq 'PK'){ $tbl.Rows.Item($r).Range.Font.Bold=1 }
      }
      $tbl.Rows.Item($tbl.Rows.Count).Range.ParagraphFormat.KeepWithNext=-1; $null=$sel.EndKey(6)
      Caption 'Bảng' "Thực thể $ten"
    }
    $doc.SaveAs2("$outDir\$name.docx",16)
    "docx: $outDir\$name.docx ($n bảng)"
    if($Pdf){ $doc.ExportAsFixedFormat("$dir\$name.pdf",17); "pdf: $dir\$name.pdf" }
  } finally { $doc.Close(0); if(-not $sharedInstance){ $word.Quit() }; [void][Runtime.InteropServices.Marshal]::ReleaseComObject($word) }
}
