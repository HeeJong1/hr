package com.heejong.hr.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.heejong.hr.entity.Attendance;

@Mapper
public interface AttendanceMapper {

    /**
     * 출근 등록
     */
    int insertCheckIn(Attendance attendance);

    /**
     * 퇴근 등록
     */
    int updateCheckOut(Attendance attendance);

    /**
     * 특정 회원의 특정 날짜 출퇴근 기록 조회
     */
    Attendance findByMemberNoAndDate(@Param("memberNo") Long memberNo, @Param("workDate") LocalDate workDate);

    /**
     * 특정 회원의 출퇴근 기록 목록 조회 (최신순)
     */
    List<Attendance> findByMemberNo(@Param("memberNo") Long memberNo, @Param("limit") Integer limit);

    /**
     * 특정 회원의 월별 출퇴근 기록 조회
     */
    List<Attendance> findByMemberNoAndMonth(@Param("memberNo") Long memberNo, @Param("year") int year, @Param("month") int month);

    /**
     * 모든 출퇴근 기록 조회 (관리자용)
     */
    List<Attendance> findAll(@Param("limit") Integer limit);

    /**
     * 특정 날짜의 모든 출퇴근 기록 조회 (관리자용)
     */
    List<Attendance> findByDate(@Param("workDate") LocalDate workDate);

    /**
     * 특정 회원의 월별 총 근무시간 조회
     */
    Integer getTotalWorkMinutes(@Param("memberNo") Long memberNo, @Param("year") int year, @Param("month") int month);

    /**
     * 출퇴근 기록 삭제
     */
    int deleteAttendance(@Param("attendanceNo") Long attendanceNo);
}
