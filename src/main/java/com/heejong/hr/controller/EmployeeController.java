package com.heejong.hr.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heejong.hr.entity.Member;
import com.heejong.hr.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 모든 직원 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEmployees() {
        try {
            List<Member> employees = employeeService.getAllEmployees();

            // 비밀번호 제외하고 반환
            List<Map<String, Object>> employeeList = employees.stream()
                    .map(member -> {
                        Map<String, Object> emp = new HashMap<>();
                        emp.put("memberNo", member.getMemberNo());
                        emp.put("id", member.getId());
                        emp.put("email", member.getEmail());
                        emp.put("name", member.getName());
                        emp.put("role", member.getRole());
                        emp.put("birthdate", member.getBirthdate() != null ? member.getBirthdate().toString() : null);
                        emp.put("age", member.getAge());
                        return emp;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("employees", employeeList);
            response.put("total", employeeList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 직원 연봉 업데이트
     */
    @PutMapping("/{memberNo}/annual-salary")
    public ResponseEntity<Map<String, Object>> updateAnnualSalary(
            @PathVariable Long memberNo,
            @RequestBody Map<String, String> body) {
        try {
            String annualSalary = body.get("annualSalary");
            if (annualSalary == null || annualSalary.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "연봉을 입력해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            employeeService.updateAnnualSalary(memberNo, annualSalary);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "연봉이 업데이트되었습니다.");

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

    /**
     * 직원 연봉 조회
     */
    @GetMapping("/{memberNo}/annual-salary")
    public ResponseEntity<Map<String, Object>> getAnnualSalary(@PathVariable Long memberNo) {
        try {
            String annualSalary = employeeService.getAnnualSalary(memberNo);

            Map<String, Object> response = new HashMap<>();
            response.put("annualSalary", annualSalary);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 연봉 기반 월급 계산
     */
    @GetMapping("/{memberNo}/monthly-salary")
    public ResponseEntity<Map<String, Object>> calculateMonthlySalary(@PathVariable Long memberNo) {
        try {
            java.math.BigDecimal monthlySalary = employeeService.calculateMonthlySalary(memberNo);

            Map<String, Object> response = new HashMap<>();
            response.put("monthlySalary", monthlySalary);
            response.put("formattedSalary", monthlySalary != null
                    ? String.format("%,d원", monthlySalary.intValue()) : null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
