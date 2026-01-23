package com.heejong.hr.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.heejong.hr.entity.Member;
import com.heejong.hr.mapper.LoginMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final LoginMapper loginMapper;

    /**
     * 모든 직원 조회
     */
    public List<Member> getAllEmployees() {
        return loginMapper.findAll();
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
}
