package com.heejong.hr.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

import com.heejong.hr.entity.LeaveRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelService {

    /**
     * 연차 신청 내역을 엑셀 파일로 변환
     */
    public byte[] exportLeaveRequestsToExcel(List<LeaveRequest> leaveRequests) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("연차 신청 내역");

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
        headerStyle.setFont(headerFont);

        // 일반 셀 스타일
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        // 헤더 행 생성
        Row headerRow = sheet.createRow(0);
        String[] columns = {"신청번호", "신청자 ID", "신청자 이름", "연차 종류", "시작일", "종료일", 
                           "일수", "사유", "상태", "신청일시", "승인일시", "승인자 코멘트"};

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터 행 생성
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        int rowNum = 1;
        for (LeaveRequest leave : leaveRequests) {
            Row row = sheet.createRow(rowNum++);

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(leave.getLeaveRequestNo());
            cell0.setCellStyle(cellStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(leave.getMemberId() != null ? leave.getMemberId() : "");
            cell1.setCellStyle(cellStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(leave.getMemberName() != null ? leave.getMemberName() : "");
            cell2.setCellStyle(cellStyle);

            Cell cell3 = row.createCell(3);
            String leaveTypeText = "";
            if ("annual".equals(leave.getLeaveType())) leaveTypeText = "연차";
            else if ("half".equals(leave.getLeaveType())) leaveTypeText = "반차";
            else if ("sick".equals(leave.getLeaveType())) leaveTypeText = "병가";
            cell3.setCellValue(leaveTypeText);
            cell3.setCellStyle(cellStyle);

            Cell cell4 = row.createCell(4);
            cell4.setCellValue(leave.getStartDate() != null ? leave.getStartDate().format(dateFormatter) : "");
            cell4.setCellStyle(cellStyle);

            Cell cell5 = row.createCell(5);
            cell5.setCellValue(leave.getEndDate() != null ? leave.getEndDate().format(dateFormatter) : "");
            cell5.setCellStyle(cellStyle);

            Cell cell6 = row.createCell(6);
            cell6.setCellValue(leave.getDays() != null ? leave.getDays().toString() : "");
            cell6.setCellStyle(cellStyle);

            Cell cell7 = row.createCell(7);
            cell7.setCellValue(leave.getReason() != null ? leave.getReason() : "");
            cell7.setCellStyle(cellStyle);

            Cell cell8 = row.createCell(8);
            String statusText = "";
            if ("pending".equals(leave.getStatus())) statusText = "대기";
            else if ("approved".equals(leave.getStatus())) statusText = "승인";
            else if ("rejected".equals(leave.getStatus())) statusText = "반려";
            cell8.setCellValue(statusText);
            cell8.setCellStyle(cellStyle);

            Cell cell9 = row.createCell(9);
            cell9.setCellValue(leave.getRequestDate() != null ? leave.getRequestDate().format(dateTimeFormatter) : "");
            cell9.setCellStyle(cellStyle);

            Cell cell10 = row.createCell(10);
            cell10.setCellValue(leave.getApprovedDate() != null ? leave.getApprovedDate().format(dateTimeFormatter) : "");
            cell10.setCellStyle(cellStyle);

            Cell cell11 = row.createCell(11);
            cell11.setCellValue(leave.getApproverComment() != null ? leave.getApproverComment() : "");
            cell11.setCellStyle(cellStyle);
        }

        // 열 너비 자동 조정
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024); // 여유 공간 추가
        }

        // 바이트 배열로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}
