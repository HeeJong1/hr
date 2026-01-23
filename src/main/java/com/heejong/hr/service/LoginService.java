package com.heejong.hr.service;

import java.time.LocalDate;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.heejong.hr.entity.Member;
import com.heejong.hr.mapper.LoginMapper;
import com.heejong.hr.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginMapper loginMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Member login(String id, String rawPassword) {
        // 1. 사용자 조회
        Member member = loginMapper.findById(id);

        if (member == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자");
        }

        // 2. 비밀번호 검증
        if (!member.matchPassword(rawPassword)) {
            throw new IllegalArgumentException("비밀번호 불일치");
        }

        // 3. JWT 토큰 생성
        String token = jwtUtil.generateToken(
                member.getEmail(),
                member.getId(),
                member.getRole()
        );

        // 4. 토큰을 Member 객체에 임시 저장 (또는 별도 반환)
        // 여기서는 간단하게 Member를 반환하고 Controller에서 토큰 생성
        return member;
    }

    public void signup(String id, String email, String password, String name, String birthdate) {
        // 1. 이메일 중복 확인
        if (loginMapper.findByEmail(email) != null) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다");
        }

        // 2. ID 중복 확인
        if (loginMapper.findById(id) != null) {
            throw new IllegalArgumentException("이미 사용 중인 ID입니다");
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 4. Member 객체 생성 및 저장
        Member member = new Member();
        member.setId(id);
        member.setEmail(email);
        member.setPassword(encodedPassword);
        member.setName(name);
        member.setRole("ROLE_USER"); // 기본 역할 설정

        // 생년월일 설정
        if (birthdate != null && !birthdate.isEmpty()) {
            member.setBirthdate(LocalDate.parse(birthdate));
        }

        int result = loginMapper.insertMember(member);

        if (result == 0) {
            throw new RuntimeException("회원가입에 실패했습니다");
        }
    }

    public void withdraw(String email, String password) {
        // 1. 사용자 조회
        Member member = loginMapper.findByEmail(email);

        if (member == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다");
        }

        // 2. 비밀번호 검증
        if (!member.matchPassword(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 3. 회원 삭제
        int result = loginMapper.deleteMember(email);

        if (result == 0) {
            throw new RuntimeException("회원탈퇴에 실패했습니다");
        }
    }
}
