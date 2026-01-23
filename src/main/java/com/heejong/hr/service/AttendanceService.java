package com.heejong.hr.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.heejong.hr.entity.Attendance;
import com.heejong.hr.mapper.AttendanceMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceMapper attendanceMapper;

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
}
