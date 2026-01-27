package com.heejong.hr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/withdraw")
    public String withdraw() {
        return "withdraw";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/leave")
    public String leavePage() {
        return "leave";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "mypage";
    }

    @GetMapping("/admin/leave")
    public String adminLeavePage() {
        return "admin-leave";
    }

    @GetMapping("/employee")
    public String employeePage() {
        return "employee";
    }

    @GetMapping("/attendance")
    public String attendancePage() {
        return "attendance";
    }

    @GetMapping("/salary")
    public String salaryPage() {
        return "salary";
    }

    @GetMapping("/leaveCalendar")
    public String leaveCalendarPage() {
        return "leave-calendar";
    }

    @GetMapping("/notice")
    public String noticePage() {
        return "notice";
    }

    @GetMapping("/noticeWrite")
    public String noticeWritePage() {
        return "noticeWrite";
    }

    @GetMapping("/boss/dashboard")
    public String bossDashboardPage() {
        return "boss-dashboard";
    }

    @GetMapping("/attendance/statistics")
    public String attendanceStatisticsPage() {
        return "attendance-statistics";
    }

    @GetMapping("/salary/statement")
    public String salaryStatementPage() {
        return "salary-statement";
    }
}
