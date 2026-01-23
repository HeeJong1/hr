package com.heejong.hr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.heejong.hr.entity.Notice;

@Mapper
public interface NoticeMapper {
    
    // 공지사항 작성
    int insertNotice(Notice notice);
    
    // 공지사항 수정
    int updateNotice(Notice notice);
    
    // 공지사항 삭제
    int deleteNotice(@Param("noticeNo") Long noticeNo);
    
    // 공지사항 상세 조회
    Notice findByNoticeNo(@Param("noticeNo") Long noticeNo);
    
    // 모든 공지사항 조회 (중요 공지 우선)
    List<Notice> findAll();
    
    // 조회수 증가
    int incrementViewCount(@Param("noticeNo") Long noticeNo);
    
    // 공지사항 검색 (제목 + 내용)
    List<Notice> searchNotices(@Param("keyword") String keyword);
    
    // 페이징 처리된 공지사항 조회
    List<Notice> findAllWithPaging(@Param("offset") int offset, @Param("limit") int limit);
    
    // 전체 공지사항 개수
    int countAll();
    
    // 검색 결과 개수
    int countByKeyword(@Param("keyword") String keyword);
    
    // 페이징 처리된 검색 결과
    List<Notice> searchNoticesWithPaging(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
}
