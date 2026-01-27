package com.heejong.hr.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.heejong.hr.entity.Attendance;
import com.heejong.hr.entity.Member;
import com.heejong.hr.mapper.AttendanceMapper;
import com.heejong.hr.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceMapper attendanceMapper;
    private final EmployeeService employeeService;

    // 정상 출근 기준 시간 (9시)
    private static final LocalTime NORMAL_CHECK_IN_TIME = LocalTime.of(9, 0);

    /**
     * 출근 등록
     */
    @Transactional
    public Attendance checkIn(Long memberNo, String memo) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 오늘 이미 출근했는지 확인
        Attendance existing = attendanceMapper.findByMemberNoAndDate(memberNo, today);
        if (existing != null) {
            throw new IllegalArgumentException("이미 출근 처리되었습니다.");
        }

        // 출근 상태 판단 (9시 기준)
        String status = "NORMAL";
        if (now.toLocalTime().isAfter(NORMAL_CHECK_IN_TIME)) {
            status = "LATE"; // 지각
        }

        Attendance attendance = new Attendance();
        attendance.setMemberNo(memberNo);
        attendance.setWorkDate(today);
        attendance.setCheckInTime(now);
        attendance.setStatus(status);
        attendance.setMemo(memo);

        attendanceMapper.insertCheckIn(attendance);

        return attendanceMapper.findByMemberNoAndDate(memberNo, today);
    }

    /**
     * 퇴근 등록
     */
    @Transactional
    public Attendance checkOut(Long memberNo) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 오늘 출근 기록 조회
        Attendance attendance = attendanceMapper.findByMemberNoAndDate(memberNo, today);
        if (attendance == null) {
            throw new IllegalArgumentException("출근 기록이 없습니다.");
        }

        if (attendance.getCheckOutTime() != null) {
            throw new IllegalArgumentException("이미 퇴근 처리되었습니다.");
        }

        // 근무 시간 계산 (분 단위)
        long workMinutes = ChronoUnit.MINUTES.between(attendance.getCheckInTime(), now);

        // 조퇴 판단 (8시간 = 480분 미만이면 조퇴)
        String status = attendance.getStatus();
        if (workMinutes < 480 && "NORMAL".equals(status)) {
            status = "EARLY_LEAVE";
        }

        attendance.setCheckOutTime(now);
        attendance.setWorkMinutes((int) workMinutes);
        attendance.setStatus(status);

        attendanceMapper.updateCheckOut(attendance);

        return attendanceMapper.findByMemberNoAndDate(memberNo, today);
    }

    /**
     * 오늘의 출퇴근 기록 조회
     */
    public Attendance getTodayAttendance(Long memberNo) {
        LocalDate today = LocalDate.now();
        return attendanceMapper.findByMemberNoAndDate(memberNo, today);
    }

    /**
     * 특정 회원의 출퇴근 기록 목록 조회
     */
    public List<Attendance> getAttendanceList(Long memberNo, Integer limit) {
        return attendanceMapper.findByMemberNo(memberNo, limit);
    }

    /**
     * 특정 회원의 월별 출퇴근 기록 조회
     */
    public List<Attendance> getMonthlyAttendance(Long memberNo, int year, int month) {
        return attendanceMapper.findByMemberNoAndMonth(memberNo, year, month);
    }

    /**
     * 모든 출퇴근 기록 조회 (관리자용)
     */
    public List<Attendance> getAllAttendance(Integer limit) {
        return attendanceMapper.findAll(limit);
    }

    /**
     * 특정 날짜의 모든 출퇴근 기록 조회 (관리자용)
     */
    public List<Attendance> getAttendanceByDate(LocalDate workDate) {
        return attendanceMapper.findByDate(workDate);
    }

    /**
     * 특정 회원의 월별 총 근무시간 조회
     */
    public Integer getTotalWorkMinutes(Long memberNo, int year, int month) {
        return attendanceMapper.getTotalWorkMinutes(memberNo, year, month);
    }

    /**
     * 월별 근무시간을 시간:분 형식으로 반환
     */
    public String getTotalWorkHours(Long memberNo, int year, int month) {
        Integer totalMinutes = getTotalWorkMinutes(memberNo, year, month);
        if (totalMinutes == null || totalMinutes == 0) {
            return "0:00";
        }
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    /**
     * 출퇴근 기록 삭제
     */
    @Transactional
    public void deleteAttendance(Long attendanceNo) {
        int deleted = attendanceMapper.deleteAttendance(attendanceNo);
        if (deleted == 0) {
            throw new IllegalArgumentException("삭제할 출퇴근 기록을 찾을 수 없습니다.");
        }
    }

    /**
     * 오늘 날짜 기준 모든 직원의 출근/미출근 현황 조회 (boss 대시보드용)
     */
    public Map<String, Object> getTodayDashboard() {
        LocalDate today = LocalDate.now();
        
        // 모든 직원 조회
        List<Member> allEmployees = employeeService.getAllEmployees();
        
        // 오늘 날짜의 출근 기록 조회
        List<Attendance> todayAttendanceList = attendanceMapper.findByDate(today);
        
        // 출근 기록을 memberNo를 키로 하는 Map으로 변환
        Map<Long, Attendance> attendanceMap = todayAttendanceList.stream()
            .collect(Collectors.toMap(Attendance::getMemberNo, attendance -> attendance));
        
        // 직원별 출근 현황 리스트 생성
        List<Map<String, Object>> employeeStatusList = allEmployees.stream()
            .map(employee -> {
                Map<String, Object> status = new HashMap<>();
                status.put("memberNo", employee.getMemberNo());
                status.put("id", employee.getId());
                status.put("name", employee.getName());
                status.put("email", employee.getEmail());
                status.put("role", employee.getRole());
                
                Attendance attendance = attendanceMap.get(employee.getMemberNo());
                
                if (attendance != null) {
                    // 출근함
                    status.put("hasAttended", true);
                    status.put("checkInTime", attendance.getCheckInTime() != null 
                        ? attendance.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm")) 
                        : null);
                    status.put("checkOutTime", attendance.getCheckOutTime() != null 
                        ? attendance.getCheckOutTime().format(DateTimeFormatter.ofPattern("HH:mm")) 
                        : null);
                    status.put("status", attendance.getStatus());
                    status.put("workMinutes", attendance.getWorkMinutes());
                    status.put("workHours", attendance.getWorkHours());
                } else {
                    // 미출근
                    status.put("hasAttended", false);
                    status.put("checkInTime", null);
                    status.put("checkOutTime", null);
                    status.put("status", "ABSENT");
                    status.put("workMinutes", null);
                    status.put("workHours", "0:00");
                }
                
                return status;
            })
            .collect(Collectors.toList());
        
        // 통계 계산
        long totalEmployees = allEmployees.size();
        long attendedCount = todayAttendanceList.size();
        long absentCount = totalEmployees - attendedCount;
        long normalCount = todayAttendanceList.stream()
            .filter(a -> "NORMAL".equals(a.getStatus()))
            .count();
        long lateCount = todayAttendanceList.stream()
            .filter(a -> "LATE".equals(a.getStatus()))
            .count();
        long earlyLeaveCount = todayAttendanceList.stream()
            .filter(a -> "EARLY_LEAVE".equals(a.getStatus()))
            .count();
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dashboard.put("employees", employeeStatusList);
        dashboard.put("statistics", Map.of(
            "total", totalEmployees,
            "attended", attendedCount,
            "absent", absentCount,
            "normal", normalCount,
            "late", lateCount,
            "earlyLeave", earlyLeaveCount
        ));
        
        return dashboard;
    }
}
