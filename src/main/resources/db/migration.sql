-- 회원 연락처 컬럼 추가 (이미 있으면 무시)
ALTER TABLE hr.member ADD COLUMN IF NOT EXISTS phone VARCHAR(50);

-- 알림 테이블
CREATE TABLE IF NOT EXISTS hr.notification (
    notification_no BIGSERIAL PRIMARY KEY,
    member_no BIGINT NULL REFERENCES hr.member(member_no) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000),
    related_id VARCHAR(100),
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 공지 읽음 테이블
CREATE TABLE IF NOT EXISTS hr.notice_read (
    member_no BIGINT NOT NULL REFERENCES hr.member(member_no) ON DELETE CASCADE,
    notice_no BIGINT NOT NULL REFERENCES hr.notice(notice_no) ON DELETE CASCADE,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (member_no, notice_no)
);

CREATE INDEX IF NOT EXISTS idx_notification_member ON hr.notification(member_no);
CREATE INDEX IF NOT EXISTS idx_notification_created ON hr.notification(created_at DESC);
