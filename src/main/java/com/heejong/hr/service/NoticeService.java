package com.heejong.hr.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.heejong.hr.entity.Notice;
import com.heejong.hr.mapper.NoticeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;

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
     * 페이징 처리된 공지사항 조회
     */
    public Map<String, Object> getNoticesWithPaging(int page, int size) {
        int offset = (page - 1) * size;
        List<Notice> notices = noticeMapper.findAllWithPaging(offset, size);
        int totalCount = noticeMapper.countAll();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        Map<String, Object> result = new HashMap<>();
        result.put("notices", notices);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalCount", totalCount);
        result.put("size", size);

        return result;
    }

    /**
     * 페이징 처리된 검색 결과
     */
    public Map<String, Object> searchNoticesWithPaging(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getNoticesWithPaging(page, size);
        }

        int offset = (page - 1) * size;
        List<Notice> notices = noticeMapper.searchNoticesWithPaging(keyword.trim(), offset, size);
        int totalCount = noticeMapper.countByKeyword(keyword.trim());
        int totalPages = (int) Math.ceil((double) totalCount / size);

        Map<String, Object> result = new HashMap<>();
        result.put("notices", notices);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalCount", totalCount);
        result.put("size", size);
        result.put("keyword", keyword.trim());

        return result;
    }
}
