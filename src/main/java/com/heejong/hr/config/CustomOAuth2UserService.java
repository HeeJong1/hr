package com.heejong.hr.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.heejong.hr.entity.Member;
import com.heejong.hr.mapper.LoginMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final LoginMapper loginMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // OAuth2 사용자 정보를 Member 형식으로 변환
        processOAuth2User(oAuth2User, registrationId);

        // OAuth2User를 커스텀하여 반환
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("registrationId", registrationId);

        return new CustomOAuth2User(oAuth2User.getAuthorities(), attributes, "email");
    }

    private Member processOAuth2User(OAuth2User oAuth2User, String registrationId) {
        String email;
        String name;

        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
        } else {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            if (name == null) {
                name = email;
            }
        }

        // 기존 회원 조회
        Member member = loginMapper.findByEmail(email);
        if (member == null) {
            // 신규 회원인 경우 (실제로는 저장 로직 필요)
            // 여기서는 조회만 수행하고, 실제 저장은 별도 서비스에서 처리
        }

        return member;
    }
}
