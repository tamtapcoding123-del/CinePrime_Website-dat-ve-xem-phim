-- Website maintenance (18/11/2026): one row edited by Admin at /admin/maintenance.
-- enabled + starts_at in the future = notice strip only (customers can still book);
-- enabled + starts_at reached (or empty) = customer pages show the maintenance card (HTTP 503), Staff per staff_mode.
-- The window stays closed after expected_end_at until an Admin turns it off; Admin is never blocked.
CREATE TABLE maintenance (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  enabled BOOLEAN NOT NULL DEFAULT FALSE,
  title VARCHAR(160) NOT NULL,
  message VARCHAR(2000) NOT NULL,
  affected VARCHAR(2000) NULL,
  starts_at DATETIME(6) NULL,
  expected_end_at DATETIME(6) NULL,
  staff_mode VARCHAR(16) NOT NULL DEFAULT 'CHECKIN',
  contact_phone VARCHAR(40) NULL,
  contact_email VARCHAR(160) NULL,
  updated_at DATETIME(6) NULL,
  updated_by BIGINT NULL,
  CONSTRAINT maintenance_staff_mode CHECK (staff_mode IN ('WORK','CHECKIN','LOCKED')),
  CONSTRAINT maintenance_window CHECK (expected_end_at IS NULL OR starts_at IS NULL OR expected_end_at > starts_at),
  CONSTRAINT maintenance_updated_by FOREIGN KEY (updated_by) REFERENCES users(id)
);

INSERT INTO maintenance(enabled, title, message, affected, staff_mode)
VALUES (FALSE, 'Website đang bảo trì',
        'CinePrime đang nâng cấp hệ thống để phục vụ bạn tốt hơn. Trong thời gian này bạn tạm thời chưa thể đặt vé trực tuyến.',
        'Đặt vé và chọn ghế trực tuyến\nThanh toán MoMo cho đơn mới\nĐăng ký tài khoản và đổi điểm thưởng', 'CHECKIN');
