package com.heejong.hr.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.heejong.hr.entity.Salary;
import com.heejong.hr.entity.SalaryPayment;
import com.heejong.hr.mapper.SalaryMapper;
import com.heejong.hr.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalaryService {

    private final SalaryMapper salaryMapper;
    private final EmployeeService employeeService;

    // ========== 급여 정보 관리 ==========

    /**
     * 급여 정보 등록
     */
    @Transactional
    public Salary createSalary(Salary salary) {
        if (salary.getStatus() == null) {
            salary.setStatus("ACTIVE");
        }
        if (salary.getEffectiveDate() == null) {
            salary.setEffectiveDate(LocalDate.now());
        }
        
        salaryMapper.insertSalary(salary);
        return salaryMapper.findByMemberNo(salary.getMemberNo());
    }

    /**
     * 급여 정보 수정
     */
    @Transactional
    public Salary updateSalary(Salary salary) {
        int updated = salaryMapper.updateSalary(salary);
        if (updated == 0) {
            throw new IllegalArgumentException("수정할 급여 정보를 찾을 수 없습니다.");
        }
        return salaryMapper.findByMemberNo(salary.getMemberNo());
    }

    /**
     * 특정 회원의 급여 정보 조회
     */
    public Salary getSalary(Long memberNo) {
        return salaryMapper.findByMemberNo(memberNo);
    }

    /**
     * 모든 급여 정보 조회
     */
    public List<Salary> getAllSalaries() {
        return salaryMapper.findAllSalaries();
    }

    /**
     * 급여 정보 삭제
     */
    @Transactional
    public void deleteSalary(Long salaryNo) {
        int deleted = salaryMapper.deleteSalary(salaryNo);
        if (deleted == 0) {
            throw new IllegalArgumentException("삭제할 급여 정보를 찾을 수 없습니다.");
        }
    }

    // ========== 급여 지급 관리 ==========

    /**
     * 급여 지급 생성 (자동 계산) - 연봉 기반
     */
    @Transactional
    public SalaryPayment createSalaryPaymentFromAnnualSalary(Long memberNo, int year, int month) {
        // 연봉 기반 월급 계산
        BigDecimal monthlySalary = employeeService.calculateMonthlySalary(memberNo);
        if (monthlySalary == null) {
            throw new IllegalArgumentException("연봉 정보가 등록되지 않았습니다.");
        }

        // 이미 해당 월 급여가 있는지 확인
        SalaryPayment existing = salaryMapper.findPaymentByMemberAndMonth(memberNo, year, month);
        if (existing != null) {
            throw new IllegalArgumentException("해당 월 급여가 이미 등록되어 있습니다.");
        }

        SalaryPayment payment = new SalaryPayment();
        payment.setMemberNo(memberNo);
        payment.setPaymentYear(year);
        payment.setPaymentMonth(month);
        payment.setBaseSalary(monthlySalary);
        payment.setPositionAllowance(BigDecimal.ZERO);
        payment.setMealAllowance(BigDecimal.ZERO);
        payment.setTransportAllowance(BigDecimal.ZERO);
        payment.setOvertimePay(BigDecimal.ZERO);
        payment.setBonus(BigDecimal.ZERO);

        // 총 지급액 계산
        BigDecimal totalAmount = monthlySalary;
        payment.setTotalAmount(totalAmount);

        // 공제액 계산
        BigDecimal incomeTax = totalAmount.multiply(new BigDecimal("0.05")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal nationalPension = totalAmount.multiply(new BigDecimal("0.045")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal healthInsurance = totalAmount.multiply(new BigDecimal("0.0335")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal employmentInsurance = totalAmount.multiply(new BigDecimal("0.008")).setScale(0, RoundingMode.HALF_UP);

        payment.setIncomeTax(incomeTax);
        payment.setNationalPension(nationalPension);
        payment.setHealthInsurance(healthInsurance);
        payment.setEmploymentInsurance(employmentInsurance);

        BigDecimal totalDeduction = incomeTax.add(nationalPension).add(healthInsurance).add(employmentInsurance);
        payment.setTotalDeduction(totalDeduction);

        // 실 지급액
        BigDecimal netAmount = totalAmount.subtract(totalDeduction);
        payment.setNetAmount(netAmount);

        payment.setPaymentDate(LocalDate.now());
        payment.setStatus("PENDING");

        salaryMapper.insertSalaryPayment(payment);

        return salaryMapper.findPaymentByMemberAndMonth(memberNo, year, month);
    }

    /**
     * 급여 지급 생성 (자동 계산)
     */
    @Transactional
    public SalaryPayment createSalaryPayment(Long memberNo, int year, int month, 
                                             Integer workDays, Integer workHours,
                                             BigDecimal overtimePay, BigDecimal bonus) {
        // 기존 급여 정보 조회
        Salary salary = salaryMapper.findByMemberNo(memberNo);
        if (salary == null) {
            throw new IllegalArgumentException("급여 정보가 등록되지 않았습니다.");
        }

        // 이미 해당 월 급여가 있는지 확인
        SalaryPayment existing = salaryMapper.findPaymentByMemberAndMonth(memberNo, year, month);
        if (existing != null) {
            throw new IllegalArgumentException("해당 월 급여가 이미 등록되어 있습니다.");
        }

        SalaryPayment payment = new SalaryPayment();
        payment.setMemberNo(memberNo);
        payment.setPaymentYear(year);
        payment.setPaymentMonth(month);
        payment.setBaseSalary(salary.getBaseSalary());
        payment.setPositionAllowance(salary.getPositionAllowance() != null ? salary.getPositionAllowance() : BigDecimal.ZERO);
        payment.setMealAllowance(salary.getMealAllowance() != null ? salary.getMealAllowance() : BigDecimal.ZERO);
        payment.setTransportAllowance(salary.getTransportAllowance() != null ? salary.getTransportAllowance() : BigDecimal.ZERO);
        payment.setOvertimePay(overtimePay != null ? overtimePay : BigDecimal.ZERO);
        payment.setBonus(bonus != null ? bonus : BigDecimal.ZERO);
        payment.setWorkDays(workDays);
        payment.setWorkHours(workHours);

        // 총 지급액 계산
        BigDecimal totalAmount = payment.getBaseSalary()
                .add(payment.getPositionAllowance())
                .add(payment.getMealAllowance())
                .add(payment.getTransportAllowance())
                .add(payment.getOvertimePay())
                .add(payment.getBonus());
        payment.setTotalAmount(totalAmount);

        // 공제액 계산 (간이 계산)
        BigDecimal incomeTax = totalAmount.multiply(new BigDecimal("0.05")).setScale(0, RoundingMode.HALF_UP); // 소득세 5%
        BigDecimal nationalPension = totalAmount.multiply(new BigDecimal("0.045")).setScale(0, RoundingMode.HALF_UP); // 국민연금 4.5%
        BigDecimal healthInsurance = totalAmount.multiply(new BigDecimal("0.0335")).setScale(0, RoundingMode.HALF_UP); // 건강보험 3.35%
        BigDecimal employmentInsurance = totalAmount.multiply(new BigDecimal("0.008")).setScale(0, RoundingMode.HALF_UP); // 고용보험 0.8%

        payment.setIncomeTax(incomeTax);
        payment.setNationalPension(nationalPension);
        payment.setHealthInsurance(healthInsurance);
        payment.setEmploymentInsurance(employmentInsurance);

        BigDecimal totalDeduction = incomeTax.add(nationalPension).add(healthInsurance).add(employmentInsurance);
        payment.setTotalDeduction(totalDeduction);

        // 실 지급액
        BigDecimal netAmount = totalAmount.subtract(totalDeduction);
        payment.setNetAmount(netAmount);

        payment.setPaymentDate(LocalDate.now());
        payment.setStatus("PENDING");

        salaryMapper.insertSalaryPayment(payment);

        return salaryMapper.findPaymentByMemberAndMonth(memberNo, year, month);
    }

    /**
     * 급여 지급 수정
     */
    @Transactional
    public SalaryPayment updateSalaryPayment(SalaryPayment payment) {
        int updated = salaryMapper.updateSalaryPayment(payment);
        if (updated == 0) {
            throw new IllegalArgumentException("수정할 급여 지급 내역을 찾을 수 없습니다.");
        }
        return salaryMapper.findPaymentById(payment.getSalaryPaymentNo());
    }

    /**
     * 급여 지급 상태 변경
     */
    @Transactional
    public SalaryPayment updatePaymentStatus(Long salaryPaymentNo, String status) {
        int updated = salaryMapper.updatePaymentStatus(salaryPaymentNo, status);
        if (updated == 0) {
            throw new IllegalArgumentException("급여 지급 내역을 찾을 수 없습니다.");
        }
        return salaryMapper.findPaymentById(salaryPaymentNo);
    }

    /**
     * 특정 급여 지급 내역 조회
     */
    public SalaryPayment getPayment(Long salaryPaymentNo) {
        return salaryMapper.findPaymentById(salaryPaymentNo);
    }

    /**
     * 특정 회원의 급여 지급 내역 조회
     */
    public List<SalaryPayment> getPaymentsByMember(Long memberNo) {
        return salaryMapper.findPaymentsByMemberNo(memberNo);
    }

    /**
     * 특정 회원의 특정 월 급여 조회
     */
    public SalaryPayment getPaymentByMemberAndMonth(Long memberNo, int year, int month) {
        return salaryMapper.findPaymentByMemberAndMonth(memberNo, year, month);
    }

    /**
     * 모든 급여 지급 내역 조회
     */
    public List<SalaryPayment> getAllPayments(Integer year, Integer month) {
        return salaryMapper.findAllPayments(year, month);
    }

    /**
     * 급여 지급 내역 삭제
     */
    @Transactional
    public void deletePayment(Long salaryPaymentNo) {
        int deleted = salaryMapper.deletePayment(salaryPaymentNo);
        if (deleted == 0) {
            throw new IllegalArgumentException("삭제할 급여 지급 내역을 찾을 수 없습니다.");
        }
    }
}
