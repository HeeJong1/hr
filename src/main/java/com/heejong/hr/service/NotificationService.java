package com.heejong.hr.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.heejong.hr.entity.Notification;
import com.heejong.hr.mapper.NotificationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public void create(Long memberNo, String type, String title, String message, String relatedId) {
        Notification n = new Notification();
        n.setMemberNo(memberNo);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedId(relatedId);
        notificationMapper.insert(n);
    }

    /** 전체 공지용 (member_no null) */
    public void createBroadcast(String type, String title, String message, String relatedId) {
        Notification n = new Notification();
        n.setMemberNo(null);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedId(relatedId);
        notificationMapper.insert(n);
    }

    public List<Notification> getByMemberNo(Long memberNo, Integer limit) {
        return notificationMapper.findByMemberNo(memberNo, limit != null ? limit : 50);
    }

    public int countUnread(Long memberNo) {
        return notificationMapper.countUnreadByMemberNo(memberNo);
    }

    public void markAsRead(Long notificationNo) {
        notificationMapper.markAsRead(notificationNo);
    }

    public void markAllAsRead(Long memberNo) {
        notificationMapper.markAllAsReadByMemberNo(memberNo);
    }
}
