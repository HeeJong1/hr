package com.heejong.hr.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.heejong.hr.entity.Attendance;
import com.heejong.hr.mapper.AttendanceMapper;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AttendanceMapper attendanceMapper;

    /**
     * 출근 통계 리포트를 엑셀 파일로 생성
     */
    public byte[] exportAttendanceReportToExcel(Long memberNo, int year, int month) throws Exception {
        List<Attendance> attendanceList = attendanceMapper.findByMemberNoAndMonth(memberNo, year, month);
        Map<String, Object> statistics = attendanceMapper.getMonthlyStatistics(memberNo, year, month);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("출근 리포트");

        // 헤더 스타일
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);

        // 일반 셀 스타일
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        int rowNum = 0;

        // 제목
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(String.format("%d년 %d월 출근 리포트", year, month));
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

        // 통계 정보
        rowNum++;
        Row statRow1 = sheet.createRow(rowNum++);
        statRow1.createCell(0).setCellValue("총 출근일수");
        statRow1.createCell(1).setCellValue(statistics.get("totalDays").toString());
        statRow1.createCell(3).setCellValue("정상 출근");
        statRow1.createCell(4).setCellValue(statistics.get("normalDays").toString());

        Row statRow2 = sheet.createRow(rowNum++);
        statRow2.createCell(0).setCellValue("지각");
        statRow2.createCell(1).setCellValue(statistics.get("lateDays").toString());
        statRow2.createCell(3).setCellValue("조퇴");
        statRow2.createCell(4).setCellValue(statistics.get("earlyLeaveDays").toString());

        Row statRow3 = sheet.createRow(rowNum++);
        statRow3.createCell(0).setCellValue("결근");
        statRow3.createCell(1).setCellValue(statistics.get("absentDays").toString());
        Integer totalMinutes = (Integer) statistics.get("totalWorkMinutes");
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        statRow3.createCell(3).setCellValue("총 근무시간");
        statRow3.createCell(4).setCellValue(String.format("%d시간 %d분", hours, minutes));

        rowNum++;

        // 헤더 행
        Row headerRow = sheet.createRow(rowNum++);
        String[] columns = {"날짜", "출근시간", "퇴근시간", "상태", "근무시간", "메모"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터 행
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Attendance attendance : attendanceList) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(attendance.getWorkDate().format(dateFormatter));
            row.createCell(1).setCellValue(attendance.getCheckInTime() != null ? 
                attendance.getCheckInTime().format(timeFormatter) : "");
            row.createCell(2).setCellValue(attendance.getCheckOutTime() != null ? 
                attendance.getCheckOutTime().format(timeFormatter) : "");
            
            String statusText = "";
            switch (attendance.getStatus()) {
                case "NORMAL": statusText = "정상"; break;
                case "LATE": statusText = "지각"; break;
                case "EARLY_LEAVE": statusText = "조퇴"; break;
                case "ABSENT": statusText = "결근"; break;
                default: statusText = attendance.getStatus();
            }
            row.createCell(3).setCellValue(statusText);
            row.createCell(4).setCellValue(attendance.getWorkHours());
            row.createCell(5).setCellValue(attendance.getMemo() != null ? attendance.getMemo() : "");
        }

        // 열 너비 자동 조정
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * 출근 통계 리포트를 PDF 파일로 생성
     */
    public byte[] exportAttendanceReportToPdf(Long memberNo, int year, int month, String memberName) throws Exception {
        List<Attendance> attendanceList = attendanceMapper.findByMemberNoAndMonth(memberNo, year, month);
        Map<String, Object> statistics = attendanceMapper.getMonthlyStatistics(memberNo, year, month);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // 제목
        Paragraph title = new Paragraph(String.format("%d년 %d월 출근 리포트 - %s", year, month, memberName))
            .setFontSize(18)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20);
        document.add(title);

        // 통계 정보
        Paragraph statsTitle = new Paragraph("통계 정보")
            .setFontSize(14)
            .setBold()
            .setMarginBottom(10);
        document.add(statsTitle);

        float[] columnWidths = {150f, 150f};
        Table statsTable = new Table(UnitValue.createPercentArray(columnWidths));
        statsTable.setWidth(UnitValue.createPercentValue(100));

        statsTable.addCell(createCell("총 출근일수", true));
        statsTable.addCell(createCell(statistics.get("totalDays").toString(), false));
        statsTable.addCell(createCell("정상 출근", true));
        statsTable.addCell(createCell(statistics.get("normalDays").toString(), false));
        statsTable.addCell(createCell("지각", true));
        statsTable.addCell(createCell(statistics.get("lateDays").toString(), false));
        statsTable.addCell(createCell("조퇴", true));
        statsTable.addCell(createCell(statistics.get("earlyLeaveDays").toString(), false));
        statsTable.addCell(createCell("결근", true));
        statsTable.addCell(createCell(statistics.get("absentDays").toString(), false));
        
        Integer totalMinutes = (Integer) statistics.get("totalWorkMinutes");
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        statsTable.addCell(createCell("총 근무시간", true));
        statsTable.addCell(createCell(String.format("%d시간 %d분", hours, minutes), false));

        document.add(statsTable);
        document.add(new Paragraph("\n"));

        // 상세 내역
        Paragraph detailTitle = new Paragraph("상세 내역")
            .setFontSize(14)
            .setBold()
            .setMarginBottom(10);
        document.add(detailTitle);

        float[] detailColumnWidths = {100f, 80f, 80f, 60f, 80f, 200f};
        Table detailTable = new Table(UnitValue.createPercentArray(detailColumnWidths));
        detailTable.setWidth(UnitValue.createPercentValue(100));

        // 헤더
        detailTable.addHeaderCell(createCell("날짜", true));
        detailTable.addHeaderCell(createCell("출근시간", true));
        detailTable.addHeaderCell(createCell("퇴근시간", true));
        detailTable.addHeaderCell(createCell("상태", true));
        detailTable.addHeaderCell(createCell("근무시간", true));
        detailTable.addHeaderCell(createCell("메모", true));

        // 데이터
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Attendance attendance : attendanceList) {
            detailTable.addCell(createCell(attendance.getWorkDate().format(dateFormatter), false));
            detailTable.addCell(createCell(attendance.getCheckInTime() != null ? 
                attendance.getCheckInTime().format(timeFormatter) : "", false));
            detailTable.addCell(createCell(attendance.getCheckOutTime() != null ? 
                attendance.getCheckOutTime().format(timeFormatter) : "", false));
            
            String statusText = "";
            switch (attendance.getStatus()) {
                case "NORMAL": statusText = "정상"; break;
                case "LATE": statusText = "지각"; break;
                case "EARLY_LEAVE": statusText = "조퇴"; break;
                case "ABSENT": statusText = "결근"; break;
                default: statusText = attendance.getStatus();
            }
            detailTable.addCell(createCell(statusText, false));
            detailTable.addCell(createCell(attendance.getWorkHours(), false));
            detailTable.addCell(createCell(attendance.getMemo() != null ? attendance.getMemo() : "", false));
        }

        document.add(detailTable);
        document.close();

        return outputStream.toByteArray();
    }

    private com.itextpdf.layout.element.Cell createCell(String text, boolean isHeader) {
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
            .add(new Paragraph(text).setFontSize(10))
            .setPadding(5);
        
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold();
        }
        
        return cell;
    }
}
