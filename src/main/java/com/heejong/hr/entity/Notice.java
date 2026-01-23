package com.heejong.hr.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Notice {
    private Long noticeNo;              // 공지사항 번호 (PK)
    private String title;               // 제목
    private String content;             // 내용
    private Long authorMemberNo;        // 작성자 회원번호 (FK)
    private Boolean isImportant;        // 중요 공지 여부
    private Integer viewCount;          // 조회수
    private LocalDateTime createdAt;    // 작성일시
    private LocalDateTime updatedAt;    // 수정일시
    
    // 조인용 필드 (작성자 정보)
    private String authorName;          // 작성자 이름
    private String authorId;            // 작성자 ID
}
