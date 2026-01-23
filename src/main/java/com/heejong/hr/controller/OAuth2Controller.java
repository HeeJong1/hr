package com.heejong.hr.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2Controller {

    @GetMapping("/login/oauth2/success")
    public ResponseEntity<Map<String, String>> oauth2Success(@RequestParam String token) {
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "로그인 성공");
        return ResponseEntity.ok(response);
    }
}
