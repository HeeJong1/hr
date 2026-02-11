package com.heejong.hr.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.heejong.hr.entity.Member;
import com.heejong.hr.service.EmployeeService;
import com.heejong.hr.service.ExcelService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ExcelService excelService;

    /**
     * 직원 조회 (페이징/검색/필터 지원, 파라미터 없으면 전체)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getEmployees(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        try {
            if (page == null || size == null || page < 1 || size < 1) {
                List<Member> employees = employeeService.getAllEmployees();
                List<Map<String, Object>> employeeList = employees.stream()
                        .map(member -> toEmployeeMap(member))
                        .collect(Collectors.toList());
                Map<String, Object> response = new HashMap<>();
                response.put("employees", employeeList);
                response.put("total", employeeList.size());
                return ResponseEntity.ok(response);
            }
            java.util.Map<String, Object> paged = employeeService.getEmployeesPaged(
                    keyword != null ? keyword : "", role != null ? role : "", page, size);
            List<Member> employees = (List<Member>) paged.get("employees");
            List<Map<String, Object>> employeeList = employees.stream()
                    .map(member -> toEmployeeMap(member))
                    .collect(Collectors.toList());
            Map<String, Object> response = new HashMap<>(paged);
            response.put("employees", employeeList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private Map<String, Object> toEmployeeMap(Member member) {
        Map<String, Object> emp = new HashMap<>();
        emp.put("memberNo", member.getMemberNo());
        emp.put("id", member.getId());
        emp.put("email", member.getEmail());
        emp.put("name", member.getName());
        emp.put("role", member.getRole());
        emp.put("birthdate", member.getBirthdate() != null ? member.getBirthdate().toString() : null);
        emp.put("age", member.getAge());
        emp.put("phone", member.getPhone());
        return emp;
    }

    /**
     * 직원 목록 엑셀 다운로드
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportEmployeesExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        try {
            List<Member> list = role != null && !role.isEmpty() || keyword != null && !keyword.isEmpty()
                    ? (List<Member>) employeeService.getEmployeesPaged(
                            keyword != null ? keyword : "", role != null ? role : "", 1, 10000).get("employees")
                    : employeeService.getAllEmployees();
            byte[] bytes = excelService.exportEmployeesToExcel(list);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "employees_" + java.time.LocalDate.now() + ".xlsx");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 직원 목록 엑셀 일괄 등록
     */
    @PostMapping("/import/excel")
    public ResponseEntity<Map<String, Object>> importEmployeesExcel(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "파일을 선택해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            Map<String, Object> result = employeeService.importEmployeesFromExcel(file.getInputStream());
            result.put("message", "일괄 등록 완료. " + result.get("insertedCount") + "명 등록, " + result.get("skippedCount") + "명 스킵.");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "엑셀 처리 중 오류: " + e.getMessage());
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
