package com.heejong.hr.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heejong.hr.entity.Notice;
import com.heejong.hr.service.NoticeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지사항 작성
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createNotice(@RequestBody Map<String, Object> body) {
        try {
            String title = body.get("title").toString();
            String content = body.get("content").toString();
            Long authorMemberNo = Long.valueOf(body.get("authorMemberNo").toString());
            Boolean isImportant = body.get("isImportant") != null ? 
                    Boolean.valueOf(body.get("isImportant").toString()) : false;

            noticeService.createNotice(title, content, authorMemberNo, isImportant);

            Map<String, String> response = new HashMap<>();
            response.put("message", "공지사항이 작성되었습니다");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 공지사항 수정
     */
    @PutMapping("/{noticeNo}")
    public ResponseEntity<Map<String, String>> updateNotice(
            @PathVariable Long noticeNo,
            @RequestBody Map<String, Object> body) {
        try {
            String title = body.get("title").toString();
            String content = body.get("content").toString();
            Boolean isImportant = body.get("isImportant") != null ? 
                    Boolean.valueOf(body.get("isImportant").toString()) : false;

            noticeService.updateNotice(noticeNo, title, content, isImportant);

            Map<String, String> response = new HashMap<>();
            response.put("message", "공지사항이 수정되었습니다");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 공지사항 삭제
     */
    @DeleteMapping("/{noticeNo}")
    public ResponseEntity<Map<String, String>> deleteNotice(@PathVariable Long noticeNo) {
        try {
            noticeService.deleteNotice(noticeNo);

            Map<String, String> response = new HashMap<>();
            response.put("message", "공지사항이 삭제되었습니다");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 공지사항 상세 조회
     */
    @GetMapping("/{noticeNo}")
    public ResponseEntity<Map<String, Object>> getNotice(@PathVariable Long noticeNo) {
        try {
            Notice notice = noticeService.getNotice(noticeNo);

            Map<String, Object> response = new HashMap<>();
            response.put("notice", notice);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 모든 공지사항 조회
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllNotices() {
        try {
            List<Notice> notices = noticeService.getAllNotices();

            Map<String, Object> response = new HashMap<>();
            response.put("notices", notices);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 공지사항 검색
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchNotices(@RequestParam String keyword) {
        try {
            List<Notice> notices = noticeService.searchNotices(keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("notices", notices);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 페이징 처리된 공지사항 조회 (memberNo 있으면 읽음 목록 포함)
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getNoticesWithPaging(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long memberNo) {
        try {
            Map<String, Object> result = noticeService.getNoticesWithPaging(page, size, memberNo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 페이징 처리된 검색 결과 (memberNo 있으면 읽음 목록 포함)
     */
    @GetMapping("/page/search")
    public ResponseEntity<Map<String, Object>> searchNoticesWithPaging(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long memberNo) {
        try {
            Map<String, Object> result = noticeService.searchNoticesWithPaging(keyword, page, size, memberNo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 공지 읽음 처리
     */
    @PostMapping("/{noticeNo}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long noticeNo,
            @RequestBody Map<String, Object> body) {
        try {
            Long memberNo = body.get("memberNo") != null ? Long.valueOf(body.get("memberNo").toString()) : null;
            if (memberNo == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "회원 정보가 필요합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            noticeService.markAsRead(memberNo, noticeNo);
            Map<String, String> response = new HashMap<>();
            response.put("message", "읽음 처리되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
