package com.heejong.hr.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.heejong.hr.entity.Member;
import com.heejong.hr.mapper.LoginMapper;
import com.heejong.hr.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final LoginMapper loginMapper;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getEmail();
        String registrationId = (String) oAuth2User.getAttributes().get("registrationId");

        // 기존 회원 조회 또는 신규 회원 생성
        Member member = loginMapper.findByEmail(email);
        if (member == null) {
            // 신규 회원 생성 (실제 구현에서는 회원 저장 로직 필요)
            member = new Member();
            member.setEmail(email);
            member.setName(oAuth2User.getName());
            member.setRole("ROLE_USER");
            member.setPassword(null); // OAuth2 로그인 사용자는 비밀번호가 없음
            // loginMapper.save(member); // 실제로는 저장 로직 필요
        }

        // JWT 토큰 생성 (member.getId()가 null일 수 있으므로 기본값 처리)
        String userId = member.getId() != null ? member.getId() : "oauth_" + email;
        String token = jwtUtil.generateToken(
                member.getEmail(),
                userId,
                member.getRole() != null ? member.getRole() : "ROLE_USER"
        );

        // 토큰을 쿼리 파라미터로 전달
        String redirectUrl = String.format("/login/oauth2/success?token=%s", token);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
