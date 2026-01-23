package com.heejong.hr.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.heejong.hr.entity.LeaveRequest;

@Mapper
public interface LeaveRequestMapper {

    // 연차 신청
    int insertLeaveRequest(LeaveRequest leaveRequest);

    // 특정 회원의 연차 신청 목록 조회
    List<LeaveRequest> findByMemberNo(@Param("memberNo") Long memberNo);

    // 모든 연차 신청 목록 조회 (관리자용)
    List<LeaveRequest> findAll();

    // 연차 신청 상세 조회
    LeaveRequest findByLeaveRequestNo(@Param("leaveRequestNo") Long leaveRequestNo);

    // 연차 신청 상태 변경 (승인/반려)
    int updateStatus(LeaveRequest leaveRequest);

    // 연차 신청 삭제
    int deleteLeaveRequest(@Param("leaveRequestNo") Long leaveRequestNo);

    // 특정 회원의 사용한 연차 일수 합계
    BigDecimal getTotalUsedDays(@Param("memberNo") Long memberNo);

    // 날짜 범위로 연차 조회 (캘린더용)
    List<LeaveRequest> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
