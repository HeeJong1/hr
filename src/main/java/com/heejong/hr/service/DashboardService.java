package com.heejong.hr.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.heejong.hr.entity.LeaveRequest;
import com.heejong.hr.entity.Member;
import com.heejong.hr.entity.Notice;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final LeaveRequestService leaveRequestService;
    private final NoticeService noticeService;

    /**
     * 홈 대시보드 요약 (직원 수, 오늘 출근률, 미결재 휴가 건수, 최근 공지)
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        List<Member> allEmployees = employeeService.getAllEmployees();
        summary.put("employeeCount", allEmployees.size());

        Map<String, Object> todayDashboard = attendanceService.getTodayDashboard();
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) todayDashboard.get("statistics");
        long total = stats != null ? ((Number) stats.get("total")).longValue() : 0;
        long attended = stats != null ? ((Number) stats.get("attended")).longValue() : 0;
        double attendanceRate = total > 0 ? (attended * 100.0 / total) : 0;
        summary.put("todayAttendanceRate", Math.round(attendanceRate * 10) / 10.0);
        summary.put("todayAttended", attended);
        summary.put("todayTotal", total);

        List<LeaveRequest> allLeave = leaveRequestService.getAllLeaveRequests();
        long pendingCount = allLeave.stream().filter(l -> "pending".equals(l.getStatus())).count();
        summary.put("pendingLeaveCount", pendingCount);

        List<Notice> notices = noticeService.getAllNotices();
        List<Map<String, Object>> recentNotices = notices.stream().limit(5).map(n -> {
            Map<String, Object> m = new HashMap<>();
            m.put("noticeNo", n.getNoticeNo());
            m.put("title", n.getTitle());
            m.put("isImportant", n.getIsImportant());
            m.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());
        summary.put("recentNotices", recentNotices);

        return summary;
    }
}
