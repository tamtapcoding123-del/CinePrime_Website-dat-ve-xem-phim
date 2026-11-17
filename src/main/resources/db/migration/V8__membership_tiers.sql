-- Membership tiers, phase 1 (16/11/2026): tier rules kept in the database, the tier cached on each customer,
-- a history of tier changes, and a tier price snapshot on every order and order line.
-- Qualifying spend = amount actually paid on PAID orders in a calendar year (Asia/Ho_Chi_Minh); tier = max(this year, last year).

CREATE TABLE membership_tiers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  rank_order INT NOT NULL UNIQUE,
  min_spend BIGINT NOT NULL,
  ticket_pct INT NOT NULL,
  combo_pct INT NOT NULL,
  color VARCHAR(20) NOT NULL,
  benefits VARCHAR(2000) NOT NULL,
  CONSTRAINT tiers_spend CHECK (min_spend >= 0 AND min_spend <= 10000000000),
  CONSTRAINT tiers_pct CHECK (ticket_pct BETWEEN 0 AND 50 AND combo_pct BETWEEN 0 AND 50)
);

INSERT INTO membership_tiers (code, name, rank_order, min_spend, ticket_pct, combo_pct, color, benefits) VALUES
('SILVER',   'Bạc',       1,        0,  0,  0, '#8a94a6', 'Tích điểm khi mua vé và bắp nước\nNhận thông báo phim mới và chương trình khuyến mãi\nƯu đãi sinh nhật cơ bản\nNhận voucher ưu đãi cho lần mua vé tiếp theo'),
('GOLD',     'Vàng',      2,  1000000,  5,  5, '#c99a2e', 'Giảm 5% giá vé xem phim\nGiảm 5% combo bắp nước\nTặng voucher sinh nhật'),
('PLATINUM', 'Bạch Kim',  3,  5000000, 10, 10, '#5b7a99', 'Giảm 10% giá vé xem phim\nGiảm 10% combo bắp nước\nTặng 1 voucher vé xem phim vào tháng sinh nhật'),
('DIAMOND',  'Kim Cương', 4, 10000000, 15, 15, '#2f7fd1', 'Giảm 15% giá vé xem phim\nGiảm 15% combo bắp nước\nTặng 2 voucher vé xem phim vào tháng sinh nhật\nTham gia các buổi chiếu dành riêng cho thành viên VIP'),
('VIP',      'VIP',       5, 20000000, 20, 20, '#b3261e', 'Giảm 20% giá vé xem phim\nGiảm 20% combo bắp nước\nTặng 3 voucher vé xem phim mỗi năm\nMiễn phí nâng cấp combo bắp nước theo chương trình\nƯu tiên đặt ghế và hỗ trợ đặt vé riêng\nMời tham dự sự kiện ra mắt phim, suất chiếu VIP\nQuà tặng sinh nhật cao cấp (cốc nước / áo)');

-- tier_review_year: the year of the last review. The first refresh in a new year may move the tier down; within a year it only moves up.
ALTER TABLE users
  ADD COLUMN tier_code VARCHAR(20) NOT NULL DEFAULT 'SILVER',
  ADD COLUMN tier_since DATETIME(6) NULL,
  ADD COLUMN tier_review_year INT NULL,
  ADD CONSTRAINT users_tier FOREIGN KEY(tier_code) REFERENCES membership_tiers(code),
  ADD INDEX users_role_tier(role, tier_code);

CREATE TABLE tier_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  from_code VARCHAR(20) NOT NULL,
  to_code VARCHAR(20) NOT NULL,
  reason VARCHAR(20) NOT NULL,
  spend BIGINT NOT NULL,
  note VARCHAR(500),
  actor_id BIGINT,
  created_at DATETIME(6) NOT NULL,
  FOREIGN KEY(user_id) REFERENCES users(id),
  FOREIGN KEY(actor_id) REFERENCES users(id),
  FOREIGN KEY(from_code) REFERENCES membership_tiers(code),
  FOREIGN KEY(to_code) REFERENCES membership_tiers(code),
  INDEX tier_history_user(user_id, created_at),
  CONSTRAINT tier_history_reason CHECK (reason IN ('UPGRADE','ANNUAL_REVIEW','ADMIN'))
);

-- orders.discount stays the whole discount (tier + voucher), so subtotal - discount = total and reports are unchanged.
ALTER TABLE orders
  ADD COLUMN tier_code VARCHAR(20) NULL,
  ADD COLUMN tier_ticket_pct INT NOT NULL DEFAULT 0,
  ADD COLUMN tier_combo_pct INT NOT NULL DEFAULT 0,
  ADD COLUMN tier_discount BIGINT NOT NULL DEFAULT 0,
  ADD CONSTRAINT orders_tier FOREIGN KEY(tier_code) REFERENCES membership_tiers(code),
  ADD CONSTRAINT orders_tier_discount CHECK (tier_discount >= 0 AND tier_discount <= discount),
  ADD CONSTRAINT orders_tier_pct CHECK (tier_ticket_pct BETWEEN 0 AND 50 AND tier_combo_pct BETWEEN 0 AND 50),
  ADD INDEX orders_user_paid(user_id, status, paid_at);

ALTER TABLE order_seats
  ADD COLUMN discount BIGINT NOT NULL DEFAULT 0,
  ADD CONSTRAINT order_seats_discount CHECK (discount >= 0 AND discount <= price);

ALTER TABLE order_products
  ADD COLUMN discount BIGINT NOT NULL DEFAULT 0,
  ADD CONSTRAINT order_products_discount CHECK (discount >= 0 AND discount <= price * quantity);
