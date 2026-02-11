package com.heejong.hr.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.heejong.hr.entity.Member;
import com.heejong.hr.mapper.LoginMapper;
import com.heejong.hr.util.EncryptionUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final LoginMapper loginMapper;
    private final EncryptionUtil encryptionUtil;
    private final ExcelService excelService;

    /**
     * 모든 직원 조회
     */
    public List<Member> getAllEmployees() {
        return loginMapper.findAll();
    }

    /**
     * 검색/필터/페이징으로 직원 목록 조회
     */
    public Map<String, Object> getEmployeesPaged(String keyword, String role, int page, int size) {
        int offset = (page - 1) * size;
        List<Member> list = loginMapper.findWithPaging(keyword, role, offset, size);
        int totalCount = loginMapper.countBySearch(keyword, role);
        int totalPages = (int) Math.ceil((double) totalCount / size);
        if (totalPages < 1) totalPages = 1;

        Map<String, Object> result = new HashMap<>();
        result.put("employees", list);
        result.put("totalCount", totalCount);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("size", size);
        result.put("keyword", keyword);
        result.put("role", role);
        return result;
    }

    /**
     * 엑셀 파일로 직원 일괄 등록 (기존 ID 있으면 스킵)
     */
    @Transactional
    public Map<String, Object> importEmployeesFromExcel(java.io.InputStream inputStream) throws Exception {
        List<Member> list = excelService.parseEmployeesFromExcel(inputStream);
        int inserted = 0, skipped = 0;
        for (Member m : list) {
            if (loginMapper.findById(m.getId()) != null) {
                skipped++;
                continue;
            }
            if (loginMapper.findByEmail(m.getEmail()) != null) {
                skipped++;
                continue;
            }
            loginMapper.insertMember(m);
            inserted++;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("insertedCount", inserted);
        result.put("skippedCount", skipped);
        result.put("totalRows", list.size());
        return result;
    }

    /**
     * 특정 직원 조회
     */
    public Member getEmployee(String id) {
        Member member = loginMapper.findById(id);
        
        if (member == null) {
            throw new IllegalArgumentException("존재하지 않는 직원입니다");
        }
        
        return member;
    }

    /**
     * 직원 연봉 업데이트 (암호화)
     */
    @Transactional
    public void updateAnnualSalary(Long memberNo, String annualSalary) {
        try {
            // 입력값 검증
            if (annualSalary == null || annualSalary.trim().isEmpty()) {
                throw new IllegalArgumentException("연봉을 입력해주세요.");
            }
            
            // 숫자 형식 검증
            try {
                Double.parseDouble(annualSalary);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("연봉은 숫자로 입력해주세요.");
            }
            
            // 연봉 암호화
            String encryptedSalary = encryptionUtil.encrypt(annualSalary.trim());
            
            int updated = loginMapper.updateAnnualSalary(memberNo, encryptedSalary);
            if (updated == 0) {
                throw new IllegalArgumentException("직원을 찾을 수 없습니다.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            // 암호화 오류를 더 명확하게 전달
            throw new RuntimeException("연봉 암호화 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 직원 연봉 조회 (복호화)
     */
    public String getAnnualSalary(Long memberNo) {
        Member member = loginMapper.findByMemberNo(memberNo);
        if (member == null || member.getAnnualSalary() == null) {
            return null;
        }
        
        try {
            return encryptionUtil.decrypt(member.getAnnualSalary());
        } catch (Exception e) {
            throw new RuntimeException("연봉 정보를 복호화할 수 없습니다.", e);
        }
    }

    /**
     * 연봉으로부터 월급 계산 (연봉 / 12)
     */
    public java.math.BigDecimal calculateMonthlySalary(Long memberNo) {
        String annualSalaryStr = getAnnualSalary(memberNo);
        if (annualSalaryStr == null || annualSalaryStr.isEmpty()) {
            return null;
        }
        
        try {
            java.math.BigDecimal annualSalary = new java.math.BigDecimal(annualSalaryStr);
            // 연봉을 12로 나눠서 월급 계산 (만원 단위로 입력받으므로 10000 곱하기)
            return annualSalary.multiply(new java.math.BigDecimal("10000"))
                    .divide(new java.math.BigDecimal("12"), 0, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("연봉 형식이 올바르지 않습니다.");
        }
    }
}
