package com.heejong.hr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.heejong.hr.entity.Notification;

@Mapper
public interface NotificationMapper {

    int insert(Notification notification);

    List<Notification> findByMemberNo(@Param("memberNo") Long memberNo, @Param("limit") Integer limit);

    int countUnreadByMemberNo(@Param("memberNo") Long memberNo);

    int markAsRead(@Param("notificationNo") Long notificationNo);

    int markAllAsReadByMemberNo(@Param("memberNo") Long memberNo);
}
