package com.heejong.hr.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.heejong.hr.entity.LeaveRequest;
import com.heejong.hr.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelService {

    /**
     * 직원 목록을 엑셀 파일로 변환
     */
    public byte[] exportEmployeesToExcel(List<Member> members) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("직원 목록");

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

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        Row headerRow = sheet.createRow(0);
        String[] columns = {"회원번호", "아이디", "이메일", "이름", "직급", "생년월일", "연락처"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int rowNum = 1;
        for (Member m : members) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(m.getMemberNo() != null ? m.getMemberNo() : 0);
            row.createCell(1).setCellValue(m.getId() != null ? m.getId() : "");
            row.createCell(2).setCellValue(m.getEmail() != null ? m.getEmail() : "");
            row.createCell(3).setCellValue(m.getName() != null ? m.getName() : "");
            row.createCell(4).setCellValue(m.getRole() != null ? m.getRole() : "");
            row.createCell(5).setCellValue(m.getBirthdate() != null ? m.getBirthdate().format(dateFormatter) : "");
            row.createCell(6).setCellValue(m.getPhone() != null ? m.getPhone() : "");
            for (int i = 0; i < columns.length; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 1024, 15000));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    /**
     * 엑셀 파일에서 직원 목록 파싱 (헤더: 아이디, 이메일, 이름, 직급, 생년월일, 연락처)
     * 비밀번호는 기본값 "password1!" 로 암호화하여 설정
     */
    public List<Member> parseEmployeesFromExcel(InputStream inputStream) throws Exception {
        List<Member> list = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            workbook.close();
            return list;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String defaultPassword = encoder.encode("password1!");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String id = getCellString(row.getCell(1));
            String email = getCellString(row.getCell(2));
            String name = getCellString(row.getCell(3));
            if (id == null || id.trim().isEmpty()) continue;
            Member m = new Member();
            m.setId(id.trim());
            m.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : id.trim() + "@hr.local");
            m.setName(name != null ? name.trim() : id.trim());
            m.setPassword(defaultPassword);
            String role = getCellString(row.getCell(4));
            m.setRole(role != null && role.contains("BOSS") ? "ROLE_BOSS" : "ROLE_USER");
            String birthStr = getCellString(row.getCell(5));
            if (birthStr != null && !birthStr.trim().isEmpty()) {
                try {
                    m.setBirthdate(LocalDate.parse(birthStr.trim()));
                } catch (Exception ignored) {}
            }
            String phone = getCellString(row.getCell(6));
            m.setPhone(phone != null ? phone.trim() : null);
            list.add(m);
        }
        workbook.close();
        return list;
    }

    private static String getCellString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return null;
        }
    }

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
