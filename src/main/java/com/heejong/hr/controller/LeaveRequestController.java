package com.heejong.hr.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heejong.hr.entity.LeaveRequest;
import com.heejong.hr.service.ExcelService;
import com.heejong.hr.service.LeaveRequestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final ExcelService excelService;

    /**
     * 연차 신청
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createLeaveRequest(@RequestBody Map<String, Object> body) {
        try {
            Long memberNo = Long.valueOf(body.get("memberNo").toString());
            String leaveType = body.get("leaveType").toString();
            LocalDate startDate = LocalDate.parse(body.get("startDate").toString());
            LocalDate endDate = LocalDate.parse(body.get("endDate").toString());
            String reason = body.get("reason").toString();

            leaveRequestService.createLeaveRequest(memberNo, leaveType, startDate, endDate, reason);

            Map<String, String> response = new HashMap<>();
            response.put("message", "연차 신청이 완료되었습니다");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 특정 회원의 연차 신청 목록 조회
     */
    @GetMapping("/member/{memberNo}")
    public ResponseEntity<Map<String, Object>> getLeaveRequestsByMember(@PathVariable Long memberNo) {
        try {
            List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByMember(memberNo);
            BigDecimal usedDays = leaveRequestService.getUsedLeaveDays(memberNo);

            Map<String, Object> response = new HashMap<>();
            response.put("leaveRequests", leaveRequests);
            response.put("usedDays", usedDays);
            response.put("totalDays", new BigDecimal("15")); // 기본 연차 15일
            response.put("remainingDays", new BigDecimal("15").subtract(usedDays));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 모든 연차 신청 목록 조회 (관리자용)
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllLeaveRequests() {
        try {
            List<LeaveRequest> leaveRequests = leaveRequestService.getAllLeaveRequests();

            Map<String, Object> response = new HashMap<>();
            response.put("leaveRequests", leaveRequests);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 연차 신청 상세 조회
     */
    @GetMapping("/{leaveRequestNo}")
    public ResponseEntity<Map<String, Object>> getLeaveRequest(@PathVariable Long leaveRequestNo) {
        try {
            LeaveRequest leaveRequest = leaveRequestService.getLeaveRequest(leaveRequestNo);

            Map<String, Object> response = new HashMap<>();
            response.put("leaveRequest", leaveRequest);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 연차 신청 승인
     */
    @PutMapping("/{leaveRequestNo}/approve")
    public ResponseEntity<Map<String, String>> approveLeaveRequest(
            @PathVariable Long leaveRequestNo,
            @RequestBody Map<String, String> body) {
        try {
            String comment = body.get("comment");
            leaveRequestService.approveLeaveRequest(leaveRequestNo, comment);

            Map<String, String> response = new HashMap<>();
            response.put("message", "연차가 승인되었습니다");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 연차 신청 반려
     */
    @PutMapping("/{leaveRequestNo}/reject")
    public ResponseEntity<Map<String, String>> rejectLeaveRequest(
            @PathVariable Long leaveRequestNo,
            @RequestBody Map<String, String> body) {
        try {
            String comment = body.get("comment");
            leaveRequestService.rejectLeaveRequest(leaveRequestNo, comment);

            Map<String, String> response = new HashMap<>();
            response.put("message", "연차가 반려되었습니다");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 연차 신청 삭제
     */
    @DeleteMapping("/{leaveRequestNo}")
    public ResponseEntity<Map<String, String>> deleteLeaveRequest(@PathVariable Long leaveRequestNo) {
        try {
            leaveRequestService.deleteLeaveRequest(leaveRequestNo);

            Map<String, String> response = new HashMap<>();
            response.put("message", "연차 신청이 삭제되었습니다");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 연차 신청 내역 엑셀 다운로드
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportLeaveRequestsToExcel() {
        try {
            List<LeaveRequest> leaveRequests = leaveRequestService.getAllLeaveRequests();
            byte[] excelFile = excelService.exportLeaveRequestsToExcel(leaveRequests);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "leave_requests_" + 
                    LocalDate.now().toString() + ".xlsx");

            return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 월별 연차 일정 조회 (캘린더용)
     */
    @GetMapping("/calendar")
    public ResponseEntity<Map<String, Object>> getLeaveCalendar(
            @RequestParam(required = false) String yearMonth) {
        try {
            YearMonth targetMonth;
            if (yearMonth != null && !yearMonth.isEmpty()) {
                targetMonth = YearMonth.parse(yearMonth);
            } else {
                targetMonth = YearMonth.now();
            }

            List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByMonth(targetMonth);

            Map<String, Object> response = new HashMap<>();
            response.put("leaveRequests", leaveRequests);
            response.put("yearMonth", targetMonth.toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
