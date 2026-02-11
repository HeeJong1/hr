package com.heejong.hr.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heejong.hr.entity.Member;
import com.heejong.hr.service.LoginService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final LoginService loginService;

    /**
     * 본인 프로필 조회 (마이페이지용)
     */
    @GetMapping("/{memberNo}/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long memberNo) {
        try {
            Member member = loginService.getMemberByMemberNo(memberNo);
            Map<String, Object> profile = new HashMap<>();
            profile.put("memberNo", member.getMemberNo());
            profile.put("id", member.getId());
            profile.put("email", member.getEmail());
            profile.put("name", member.getName());
            profile.put("role", member.getRole());
            profile.put("birthdate", member.getBirthdate() != null ? member.getBirthdate().toString() : null);
            profile.put("phone", member.getPhone());
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 본인 프로필 수정 (이름, 연락처)
     */
    @PutMapping("/{memberNo}/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            @PathVariable Long memberNo,
            @RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            String phone = body.get("phone");
            loginService.updateProfile(memberNo, name, phone);
            Map<String, String> response = new HashMap<>();
            response.put("message", "프로필이 수정되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/{memberNo}/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long memberNo,
            @RequestBody Map<String, String> body) {
        try {
            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");
            if (currentPassword == null || newPassword == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "현재 비밀번호와 새 비밀번호를 입력해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            loginService.changePassword(memberNo, currentPassword, newPassword);
            Map<String, String> response = new HashMap<>();
            response.put("message", "비밀번호가 변경되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
