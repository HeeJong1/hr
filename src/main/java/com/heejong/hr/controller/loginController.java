package com.heejong.hr.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heejong.hr.entity.Member;
import com.heejong.hr.service.LoginService;
import com.heejong.hr.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class loginController {

    private final LoginService loginService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        try {
            String id = body.get("id");
            String password = body.get("password");

            Member member = loginService.login(id, password);

            // JWT 토큰 생성
            String token = jwtUtil.generateToken(
                    member.getEmail(),
                    member.getId(),
                    member.getRole()
            );

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "로그인 성공");
            response.put("memberNo", String.valueOf(member.getMemberNo()));
            response.put("id", member.getId());
            response.put("email", member.getEmail());
            response.put("name", member.getName());
            response.put("role", member.getRole());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace(); // 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody Map<String, String> body) {
        try {
            String id = body.get("id");
            String email = body.get("email");
            String password = body.get("password");
            String name = body.get("name");
            String birthdate = body.get("birthdate");

            loginService.signup(id, email, password, name, birthdate);

            Map<String, String> response = new HashMap<>();
            response.put("message", "회원가입 성공");

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

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, String>> withdraw(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");

            loginService.withdraw(email, password);

            Map<String, String> response = new HashMap<>();
            response.put("message", "회원탈퇴 성공");

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

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // JWT는 stateless이므로 클라이언트에서 토큰을 삭제하면 됨
        // 필요시 블랙리스트 관리 로직 추가 가능
        Map<String, String> response = new HashMap<>();
        response.put("message", "로그아웃 성공");
        return ResponseEntity.ok(response);
    }
}
