package com.heejong.hr.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heejong.hr.entity.Attendance;
import com.heejong.hr.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 출근 등록
     */
    @PostMapping("/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestBody Map<String, Object> body) {
        try {
            Long memberNo = Long.valueOf(body.get("memberNo").toString());
            String memo = body.get("memo") != null ? body.get("memo").toString() : null;

            Attendance attendance = attendanceService.checkIn(memberNo, memo);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "출근 처리되었습니다.");
            response.put("attendance", attendance);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 퇴근 등록
     */
    @PostMapping("/check-out")
    public ResponseEntity<Map<String, Object>> checkOut(@RequestBody Map<String, Object> body) {
        try {
            Long memberNo = Long.valueOf(body.get("memberNo").toString());

            Attendance attendance = attendanceService.checkOut(memberNo);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "퇴근 처리되었습니다.");
            response.put("attendance", attendance);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 오늘의 출퇴근 기록 조회
     */
    @GetMapping("/today/{memberNo}")
    public ResponseEntity<Map<String, Object>> getTodayAttendance(@PathVariable Long memberNo) {
        try {
            Attendance attendance = attendanceService.getTodayAttendance(memberNo);

            Map<String, Object> response = new HashMap<>();
            response.put("attendance", attendance);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 특정 회원의 출퇴근 기록 목록 조회
     */
    @GetMapping("/member/{memberNo}")
    public ResponseEntity<Map<String, Object>> getAttendanceList(
            @PathVariable Long memberNo,
            @RequestParam(required = false) Integer limit) {
        try {
            List<Attendance> attendanceList = attendanceService.getAttendanceList(memberNo, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("attendanceList", attendanceList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 특정 회원의 월별 출퇴근 기록 조회
     */
    @GetMapping("/member/{memberNo}/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyAttendance(
            @PathVariable Long memberNo,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            List<Attendance> attendanceList = attendanceService.getMonthlyAttendance(memberNo, year, month);
            Integer totalMinutes = attendanceService.getTotalWorkMinutes(memberNo, year, month);
            String totalHours = attendanceService.getTotalWorkHours(memberNo, year, month);

            Map<String, Object> response = new HashMap<>();
            response.put("attendanceList", attendanceList);
            response.put("totalMinutes", totalMinutes);
            response.put("totalHours", totalHours);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 모든 출퇴근 기록 조회 (관리자용)
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllAttendance(@RequestParam(required = false) Integer limit) {
        try {
            List<Attendance> attendanceList = attendanceService.getAllAttendance(limit);

            Map<String, Object> response = new HashMap<>();
            response.put("attendanceList", attendanceList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 특정 날짜의 모든 출퇴근 기록 조회 (관리자용)
     */
    @GetMapping("/date/{workDate}")
    public ResponseEntity<Map<String, Object>> getAttendanceByDate(@PathVariable String workDate) {
        try {
            LocalDate date = LocalDate.parse(workDate);
            List<Attendance> attendanceList = attendanceService.getAttendanceByDate(date);

            Map<String, Object> response = new HashMap<>();
            response.put("attendanceList", attendanceList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 출퇴근 기록 삭제
     */
    @DeleteMapping("/{attendanceNo}")
    public ResponseEntity<Map<String, String>> deleteAttendance(@PathVariable Long attendanceNo) {
        try {
            attendanceService.deleteAttendance(attendanceNo);

            Map<String, String> response = new HashMap<>();
            response.put("message", "출퇴근 기록이 삭제되었습니다.");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
