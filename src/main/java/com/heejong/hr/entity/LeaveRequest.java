package com.heejong.hr.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveRequest {
    private Long leaveRequestNo;      // 신청번호 (PK)
    private Long memberNo;            // 회원번호 (FK)
    private String leaveType;         // 연차 종류 (annual, half, sick)
    private LocalDate startDate;      // 시작일
    private LocalDate endDate;        // 종료일
    private BigDecimal days;          // 일수
    private String reason;            // 사유
    private String status;            // 상태 (pending, approved, rejected)
    private LocalDateTime requestDate;  // 신청일시
    private LocalDateTime approvedDate; // 승인/반려 일시
    private String approverComment;   // 승인자 코멘트
    
    // 조인용 필드 (회원 정보)
    private String memberId;          // 회원 ID
    private String memberName;        // 회원 이름
}
