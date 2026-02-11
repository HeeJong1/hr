package com.heejong.hr.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Notification {
    private Long notificationNo;
    private Long memberNo;       // null이면 전체 공지
    private String type;         // LEAVE_APPROVED, LEAVE_REJECTED, SALARY_PAID, NOTICE_NEW
    private String title;
    private String message;
    private String relatedId;    // notice_no, leave_request_no 등
    private Boolean isRead;
    private LocalDateTime createdAt;
}
