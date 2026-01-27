package com.heejong.hr.entity;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member implements UserDetails {

    private Long memberNo;      // 회원번호 (PK)
    private String id;          // 문자열 ID
    private String email;
    private String password;
    private String name;
    private String role;
    private LocalDate birthdate; // 생년월일
    private String annualSalary; // 연봉 (암호화된 값)

    // 나이 계산
    public Integer getAge() {
        if (birthdate == null) {
            return null;
        }
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    public boolean matchPassword(String rawPassword) {
        return new BCryptPasswordEncoder().matches(rawPassword, this.password);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
