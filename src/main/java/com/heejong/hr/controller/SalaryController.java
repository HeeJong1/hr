package com.heejong.hr.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heejong.hr.entity.Salary;
import com.heejong.hr.entity.SalaryPayment;
import com.heejong.hr.service.SalaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/salary")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    // ========== 급여 정보 관리 ==========

    /**
     * 급여 정보 등록
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSalary(@RequestBody Salary salary) {
        try {
            Salary created = salaryService.createSalary(salary);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "급여 정보가 등록되었습니다.");
            response.put("salary", created);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 급여 정보 수정
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateSalary(@RequestBody Salary salary) {
        try {
            Salary updated = salaryService.updateSalary(salary);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "급여 정보가 수정되었습니다.");
            response.put("salary", updated);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 특정 회원의 급여 정보 조회
     */
    @GetMapping("/member/{memberNo}")
    public ResponseEntity<Map<String, Object>> getSalary(@PathVariable Long memberNo) {
        try {
            Salary salary = salaryService.getSalary(memberNo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("salary", salary);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 모든 급여 정보 조회
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllSalaries() {
        try {
            List<Salary> salaries = salaryService.getAllSalaries();
            
            Map<String, Object> response = new HashMap<>();
            response.put("salaries", salaries);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 급여 정보 삭제
     */
    @DeleteMapping("/{salaryNo}")
    public ResponseEntity<Map<String, String>> deleteSalary(@PathVariable Long salaryNo) {
        try {
            salaryService.deleteSalary(salaryNo);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "급여 정보가 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== 급여 지급 관리 ==========

    /**
     * 급여 지급 생성
     */
    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody Map<String, Object> body) {
        try {
            Long memberNo = Long.valueOf(body.get("memberNo").toString());
            int year = Integer.parseInt(body.get("year").toString());
            int month = Integer.parseInt(body.get("month").toString());
            Integer workDays = body.get("workDays") != null ? Integer.parseInt(body.get("workDays").toString()) : null;
            Integer workHours = body.get("workHours") != null ? Integer.parseInt(body.get("workHours").toString()) : null;
            BigDecimal overtimePay = body.get("overtimePay") != null ? new BigDecimal(body.get("overtimePay").toString()) : BigDecimal.ZERO;
            BigDecimal bonus = body.get("bonus") != null ? new BigDecimal(body.get("bonus").toString()) : BigDecimal.ZERO;

            SalaryPayment payment = salaryService.createSalaryPayment(memberNo, year, month, workDays, workHours, overtimePay, bonus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "급여가 생성되었습니다.");
            response.put("payment", payment);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 급여 지급 상태 변경
     */
    @PutMapping("/payment/{salaryPaymentNo}/status")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(
            @PathVariable Long salaryPaymentNo,
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            SalaryPayment payment = salaryService.updatePaymentStatus(salaryPaymentNo, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "급여 상태가 변경되었습니다.");
            response.put("payment", payment);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 특정 급여 지급 내역 조회
     */
    @GetMapping("/payment/{salaryPaymentNo}")
    public ResponseEntity<Map<String, Object>> getPayment(@PathVariable Long salaryPaymentNo) {
        try {
            SalaryPayment payment = salaryService.getPayment(salaryPaymentNo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("payment", payment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 특정 회원의 급여 지급 내역 조회
     */
    @GetMapping("/payment/member/{memberNo}")
    public ResponseEntity<Map<String, Object>> getPaymentsByMember(@PathVariable Long memberNo) {
        try {
            List<SalaryPayment> payments = salaryService.getPaymentsByMember(memberNo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("payments", payments);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 모든 급여 지급 내역 조회
     */
    @GetMapping("/payment/all")
    public ResponseEntity<Map<String, Object>> getAllPayments(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            List<SalaryPayment> payments = salaryService.getAllPayments(year, month);
            
            Map<String, Object> response = new HashMap<>();
            response.put("payments", payments);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 급여 지급 내역 삭제
     */
    @DeleteMapping("/payment/{salaryPaymentNo}")
    public ResponseEntity<Map<String, String>> deletePayment(@PathVariable Long salaryPaymentNo) {
        try {
            salaryService.deletePayment(salaryPaymentNo);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "급여 지급 내역이 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 연봉 기반 월급 지급 생성
     */
    @PostMapping("/payment/annual-salary")
    public ResponseEntity<Map<String, Object>> createPaymentFromAnnualSalary(@RequestBody Map<String, Object> body) {
        try {
            Long memberNo = Long.valueOf(body.get("memberNo").toString());
            int year = Integer.parseInt(body.get("year").toString());
            int month = Integer.parseInt(body.get("month").toString());

            SalaryPayment payment = salaryService.createSalaryPaymentFromAnnualSalary(memberNo, year, month);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "연봉 기반 급여가 생성되었습니다.");
            response.put("payment", payment);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
