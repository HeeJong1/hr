package com.heejong.hr.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.heejong.hr.entity.LeaveRequest;
import com.heejong.hr.mapper.LeaveRequestMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestMapper leaveRequestMapper;

    /**
     * 연차 신청
     */
    public void createLeaveRequest(Long memberNo, String leaveType, LocalDate startDate,
            LocalDate endDate, String reason) {
        // 일수 계산
        BigDecimal days = calculateDays(leaveType, startDate, endDate);

        // LeaveRequest 객체 생성
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setMemberNo(memberNo);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
        leaveRequest.setDays(days);
        leaveRequest.setReason(reason);
        leaveRequest.setStatus("pending");

        int result = leaveRequestMapper.insertLeaveRequest(leaveRequest);

        if (result == 0) {
            throw new RuntimeException("연차 신청에 실패했습니다");
        }
    }

    /**
     * 특정 회원의 연차 신청 목록 조회
     */
    public List<LeaveRequest> getLeaveRequestsByMember(Long memberNo) {
        return leaveRequestMapper.findByMemberNo(memberNo);
    }

    /**
     * 모든 연차 신청 목록 조회 (관리자용)
     */
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestMapper.findAll();
    }

    /**
     * 연차 신청 상세 조회
     */
    public LeaveRequest getLeaveRequest(Long leaveRequestNo) {
        LeaveRequest leaveRequest = leaveRequestMapper.findByLeaveRequestNo(leaveRequestNo);

        if (leaveRequest == null) {
            throw new IllegalArgumentException("존재하지 않는 연차 신청입니다");
        }

        return leaveRequest;
    }

    /**
     * 연차 신청 승인
     */
    public void approveLeaveRequest(Long leaveRequestNo, String comment) {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setLeaveRequestNo(leaveRequestNo);
        leaveRequest.setStatus("approved");
        leaveRequest.setApprovedDate(LocalDateTime.now());
        leaveRequest.setApproverComment(comment);

        int result = leaveRequestMapper.updateStatus(leaveRequest);

        if (result == 0) {
            throw new RuntimeException("연차 승인에 실패했습니다");
        }
    }

    /**
     * 연차 신청 반려
     */
    public void rejectLeaveRequest(Long leaveRequestNo, String comment) {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setLeaveRequestNo(leaveRequestNo);
        leaveRequest.setStatus("rejected");
        leaveRequest.setApprovedDate(LocalDateTime.now());
        leaveRequest.setApproverComment(comment);

        int result = leaveRequestMapper.updateStatus(leaveRequest);

        if (result == 0) {
            throw new RuntimeException("연차 반려에 실패했습니다");
        }
    }

    /**
     * 연차 신청 삭제
     */
    public void deleteLeaveRequest(Long leaveRequestNo) {
        int result = leaveRequestMapper.deleteLeaveRequest(leaveRequestNo);

        if (result == 0) {
            throw new RuntimeException("연차 신청 삭제에 실패했습니다");
        }
    }

    /**
     * 사용한 연차 일수 조회
     */
    public BigDecimal getUsedLeaveDays(Long memberNo) {
        BigDecimal usedDays = leaveRequestMapper.getTotalUsedDays(memberNo);
        return usedDays != null ? usedDays : BigDecimal.ZERO;
    }

    /**
     * 월별 연차 일정 조회 (승인된 것만)
     */
    public List<LeaveRequest> getLeaveRequestsByMonth(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return leaveRequestMapper.findByDateRange(startDate, endDate);
    }

    /**
     * 일수 계산 (연차 종류에 따라)
     */
    private BigDecimal calculateDays(String leaveType, LocalDate startDate, LocalDate endDate) {
        if ("half".equals(leaveType)) {
            return new BigDecimal("0.5");
        }

        // 시작일과 종료일 사이의 일수 계산 (양 끝 포함)
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return new BigDecimal(daysBetween);
    }
}
