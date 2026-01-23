package com.heejong.hr.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Salary {

    private Long salaryNo;              // 급여 정보 번호 (PK)
    private Long memberNo;              // 회원번호 (FK)
    private BigDecimal baseSalary;      // 기본급
    private BigDecimal positionAllowance; // 직책수당
    private BigDecimal mealAllowance;   // 식대
    private BigDecimal transportAllowance; // 교통비
    private String accountBank;         // 은행명
    private String accountNumber;       // 계좌번호
    private LocalDate effectiveDate;    // 적용일
    private String status;              // 상태 (ACTIVE: 활성, INACTIVE: 비활성)

    // 조인용 필드
    private String memberId;            // 회원 ID
    private String memberName;          // 회원 이름

    // 총 급여 계산
    public BigDecimal getTotalSalary() {
        BigDecimal total = baseSalary != null ? baseSalary : BigDecimal.ZERO;
        if (positionAllowance != null) total = total.add(positionAllowance);
        if (mealAllowance != null) total = total.add(mealAllowance);
        if (transportAllowance != null) total = total.add(transportAllowance);
        return total;
    }
}
