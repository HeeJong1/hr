package com.heejong.hr.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heejong.hr.entity.Notification;
import com.heejong.hr.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/member/{memberNo}")
    public ResponseEntity<Map<String, Object>> getByMember(
            @PathVariable Long memberNo,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<Notification> list = notificationService.getByMemberNo(memberNo, limit);
            int unreadCount = notificationService.countUnread(memberNo);
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", list);
            response.put("unreadCount", unreadCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/{notificationNo}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long notificationNo) {
        try {
            notificationService.markAsRead(notificationNo);
            Map<String, String> response = new HashMap<>();
            response.put("message", "읽음 처리되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/member/{memberNo}/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable Long memberNo) {
        try {
            notificationService.markAllAsRead(memberNo);
            Map<String, String> response = new HashMap<>();
            response.put("message", "모두 읽음 처리되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
