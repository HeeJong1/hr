package com.heejong.hr.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryPayment {

    private Long salaryPaymentNo;       // 급여 지급 번호 (PK)
    private Long memberNo;              // 회원번호 (FK)
    private Integer paymentYear;        // 지급년도
    private Integer paymentMonth;       // 지급월
    private BigDecimal baseSalary;      // 기본급
    private BigDecimal positionAllowance; // 직책수당
    private BigDecimal mealAllowance;   // 식대
    private BigDecimal transportAllowance; // 교통비
    private BigDecimal overtimePay;     // 초과근무수당
    private BigDecimal bonus;           // 상여금
    private BigDecimal totalAmount;     // 총 지급액
    private BigDecimal incomeTax;       // 소득세
    private BigDecimal nationalPension; // 국민연금
    private BigDecimal healthInsurance; // 건강보험
    private BigDecimal employmentInsurance; // 고용보험
    private BigDecimal totalDeduction;  // 총 공제액
    private BigDecimal netAmount;       // 실 지급액
    private LocalDate paymentDate;      // 지급일
    private String status;              // 상태 (PENDING: 대기, PAID: 지급완료, CANCELLED: 취소)
    private Integer workDays;           // 근무일수
    private Integer workHours;          // 근무시간

    // 조인용 필드
    private String memberId;            // 회원 ID
    private String memberName;          // 회원 이름
}
