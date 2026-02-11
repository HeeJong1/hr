package com.heejong.hr.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.heejong.hr.entity.Notice;
import com.heejong.hr.mapper.NoticeMapper;
import com.heejong.hr.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;
    private final NotificationService notificationService;

    /**
     * 공지사항 작성
     */
    public void createNotice(String title, String content, Long authorMemberNo, Boolean isImportant) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setAuthorMemberNo(authorMemberNo);
        notice.setIsImportant(isImportant != null ? isImportant : false);

        int result = noticeMapper.insertNotice(notice);

        if (result == 0) {
            throw new RuntimeException("공지사항 작성에 실패했습니다");
        }
        notificationService.createBroadcast("NOTICE_NEW", "새 공지", title, null);
    }

    /**
     * 공지사항 수정
     */
    public void updateNotice(Long noticeNo, String title, String content, Boolean isImportant) {
        Notice notice = new Notice();
        notice.setNoticeNo(noticeNo);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setIsImportant(isImportant != null ? isImportant : false);

        int result = noticeMapper.updateNotice(notice);

        if (result == 0) {
            throw new RuntimeException("공지사항 수정에 실패했습니다");
        }
    }

    /**
     * 공지사항 삭제
     */
    public void deleteNotice(Long noticeNo) {
        int result = noticeMapper.deleteNotice(noticeNo);

        if (result == 0) {
            throw new RuntimeException("공지사항 삭제에 실패했습니다");
        }
    }

    /**
     * 공지사항 상세 조회 (조회수 증가)
     */
    public Notice getNotice(Long noticeNo) {
        // 조회수 증가
        noticeMapper.incrementViewCount(noticeNo);

        Notice notice = noticeMapper.findByNoticeNo(noticeNo);

        if (notice == null) {
            throw new IllegalArgumentException("존재하지 않는 공지사항입니다");
        }

        return notice;
    }

    /**
     * 공지 읽음 처리
     */
    public void markAsRead(Long memberNo, Long noticeNo) {
        noticeMapper.insertNoticeRead(memberNo, noticeNo);
    }

    /**
     * 회원이 읽은 공지 번호 목록
     */
    public java.util.List<Long> getReadNoticeNos(Long memberNo) {
        return noticeMapper.findReadNoticeNosByMember(memberNo);
    }

    /**
     * 모든 공지사항 조회
     */
    public List<Notice> getAllNotices() {
        return noticeMapper.findAll();
    }

    /**
     * 공지사항 검색
     */
    public List<Notice> searchNotices(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllNotices();
        }
        return noticeMapper.searchNotices(keyword.trim());
    }

    /**
     * 페이징 처리된 공지사항 조회 (memberNo 있으면 읽음 목록 포함)
     */
    public Map<String, Object> getNoticesWithPaging(int page, int size, Long memberNo) {
        int offset = (page - 1) * size;
        List<Notice> notices = noticeMapper.findAllWithPaging(offset, size);
        int totalCount = noticeMapper.countAll();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        if (totalPages < 1) totalPages = 1;

        Map<String, Object> result = new HashMap<>();
        result.put("notices", notices);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalCount", totalCount);
        result.put("size", size);
        if (memberNo != null) {
            result.put("readNoticeNos", noticeMapper.findReadNoticeNosByMember(memberNo));
        }
        return result;
    }

    /**
     * 페이징 처리된 검색 결과 (memberNo 있으면 읽음 목록 포함)
     */
    public Map<String, Object> searchNoticesWithPaging(String keyword, int page, int size, Long memberNo) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getNoticesWithPaging(page, size, memberNo);
        }

        int offset = (page - 1) * size;
        List<Notice> notices = noticeMapper.searchNoticesWithPaging(keyword.trim(), offset, size);
        int totalCount = noticeMapper.countByKeyword(keyword.trim());
        int totalPages = (int) Math.ceil((double) totalCount / size);
        if (totalPages < 1) totalPages = 1;

        Map<String, Object> result = new HashMap<>();
        result.put("notices", notices);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalCount", totalCount);
        result.put("size", size);
        result.put("keyword", keyword.trim());
        if (memberNo != null) {
            result.put("readNoticeNos", noticeMapper.findReadNoticeNosByMember(memberNo));
        }
        return result;
    }
}
