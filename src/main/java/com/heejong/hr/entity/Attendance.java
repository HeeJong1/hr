package com.heejong.hr.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Attendance {

    private Long attendanceNo;      // 출퇴근 번호 (PK)
    private Long memberNo;          // 회원번호 (FK)
    private LocalDate workDate;     // 근무일
    private LocalDateTime checkInTime;   // 출근 시간
    private LocalDateTime checkOutTime;  // 퇴근 시간
    private String status;          // 상태 (NORMAL: 정상, LATE: 지각, EARLY_LEAVE: 조퇴, ABSENT: 결근)
    private Integer workMinutes;    // 근무 시간 (분)
    private String memo;            // 메모

    // 조인용 필드
    private String memberId;        // 회원 ID
    private String memberName;      // 회원 이름

    // 근무 시간 계산 (시간:분 형식)
    public String getWorkHours() {
        if (workMinutes == null) {
            return "0:00";
        }
        int hours = workMinutes / 60;
        int minutes = workMinutes % 60;
        return String.format("%d:%02d", hours, minutes);
    }
}
