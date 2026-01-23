package com.heejong.hr.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
