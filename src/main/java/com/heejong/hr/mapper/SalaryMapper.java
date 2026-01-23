package com.heejong.hr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.heejong.hr.entity.Salary;
import com.heejong.hr.entity.SalaryPayment;

@Mapper
public interface SalaryMapper {

    // ========== 급여 정보 관리 ==========
    
    /**
     * 급여 정보 등록
     */
    int insertSalary(Salary salary);

    /**
     * 급여 정보 수정
     */
    int updateSalary(Salary salary);

    /**
     * 특정 회원의 급여 정보 조회
     */
    Salary findByMemberNo(@Param("memberNo") Long memberNo);

    /**
     * 모든 급여 정보 조회
     */
    List<Salary> findAllSalaries();

    /**
     * 급여 정보 삭제
     */
    int deleteSalary(@Param("salaryNo") Long salaryNo);

    // ========== 급여 지급 관리 ==========

    /**
     * 급여 지급 등록
     */
    int insertSalaryPayment(SalaryPayment payment);

    /**
     * 급여 지급 수정
     */
    int updateSalaryPayment(SalaryPayment payment);

    /**
     * 급여 지급 상태 변경
     */
    int updatePaymentStatus(@Param("salaryPaymentNo") Long salaryPaymentNo, @Param("status") String status);

    /**
     * 특정 급여 지급 내역 조회
     */
    SalaryPayment findPaymentById(@Param("salaryPaymentNo") Long salaryPaymentNo);

    /**
     * 특정 회원의 급여 지급 내역 조회
     */
    List<SalaryPayment> findPaymentsByMemberNo(@Param("memberNo") Long memberNo);

    /**
     * 특정 회원의 특정 월 급여 지급 내역 조회
     */
    SalaryPayment findPaymentByMemberAndMonth(@Param("memberNo") Long memberNo, 
                                                @Param("year") int year, 
                                                @Param("month") int month);

    /**
     * 모든 급여 지급 내역 조회
     */
    List<SalaryPayment> findAllPayments(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 급여 지급 내역 삭제
     */
    int deletePayment(@Param("salaryPaymentNo") Long salaryPaymentNo);
}
