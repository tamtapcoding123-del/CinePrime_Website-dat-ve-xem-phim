-- Membership phases 2–4 (16/11/2026): voucher wallet and point redemption, birthday/yearly/next-purchase vouchers per tier,
-- merchandise with stock and tier gifts, free combo upgrade, member-only showtimes and early sales for high tiers.

-- Birthday: the customer sets it once (Admin can correct it). Birthday benefits need the date saved before the birth month starts.
ALTER TABLE users
  ADD COLUMN birth_date DATE NULL,
  ADD COLUMN birth_date_set_at DATETIME(6) NULL;

-- Voucher templates. PUBLIC = typed code shared by everyone (old behaviour). PERSONAL = never typed directly; copies are issued
-- into customer wallets (birthday, yearly, next purchase, campaign, Admin, or bought with points when points_cost > 0).
-- scope: ORDER (whole order), TICKET (seats only), COMBO (food and drink only), FREE_TICKET (one non-couple seat, capped by maximum),
-- COMBO_UPGRADE (value = number of combos charged at the price of the smaller combo that upgrades to them).
ALTER TABLE vouchers
  ADD COLUMN audience VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
  ADD COLUMN scope VARCHAR(20) NOT NULL DEFAULT 'ORDER',
  ADD COLUMN min_tier VARCHAR(20) NULL,
  ADD COLUMN points_cost INT NOT NULL DEFAULT 0,
  ADD COLUMN valid_days INT NOT NULL DEFAULT 30,
  ADD CONSTRAINT vouchers_min_tier FOREIGN KEY(min_tier) REFERENCES membership_tiers(code),
  ADD CONSTRAINT vouchers_audience CHECK (audience IN ('PUBLIC','PERSONAL')),
  ADD CONSTRAINT vouchers_scope CHECK (scope IN ('ORDER','TICKET','COMBO','FREE_TICKET','COMBO_UPGRADE')),
  ADD CONSTRAINT vouchers_points CHECK (points_cost >= 0 AND (points_cost = 0 OR audience = 'PERSONAL')),
  ADD CONSTRAINT vouchers_valid_days CHECK (valid_days BETWEEN 1 AND 366);

-- One wallet copy per (customer, campaign key): re-running a job or retrying a request never issues twice.
-- A copy is usable while ISSUED and not expired; order_id/reserved_until hold it for one pending order (same 5 minutes as the seats).
CREATE TABLE user_vouchers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  voucher_id BIGINT NOT NULL,
  code VARCHAR(40) NOT NULL UNIQUE,
  source VARCHAR(20) NOT NULL,
  campaign_key VARCHAR(120) NOT NULL,
  status VARCHAR(20) NOT NULL,
  issued_at DATETIME(6) NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  order_id BIGINT NULL,
  reserved_until DATETIME(6) NULL,
  used_at DATETIME(6) NULL,
  actor_id BIGINT NULL,
  FOREIGN KEY(user_id) REFERENCES users(id),
  FOREIGN KEY(voucher_id) REFERENCES vouchers(id),
  FOREIGN KEY(order_id) REFERENCES orders(id),
  FOREIGN KEY(actor_id) REFERENCES users(id),
  UNIQUE KEY user_vouchers_campaign(user_id, campaign_key),
  INDEX user_vouchers_wallet(user_id, status, expires_at),
  CONSTRAINT user_vouchers_status CHECK (status IN ('ISSUED','USED','REVOKED')),
  CONSTRAINT user_vouchers_source CHECK (source IN ('BIRTHDAY','YEARLY','NEXT','REDEEM','CAMPAIGN','ADMIN'))
);

ALTER TABLE orders
  ADD COLUMN user_voucher_id BIGINT NULL,
  ADD CONSTRAINT orders_user_voucher FOREIGN KEY(user_voucher_id) REFERENCES user_vouchers(id);

-- Points ledger: redemptions and Admin adjustments have no order; ref makes each one idempotent.
ALTER TABLE point_ledger DROP CHECK point_ledger_kind;
ALTER TABLE point_ledger
  MODIFY order_id BIGINT NULL,
  ADD COLUMN ref VARCHAR(120) NULL,
  ADD COLUMN note VARCHAR(500) NULL,
  ADD COLUMN actor_id BIGINT NULL,
  ADD UNIQUE KEY point_ledger_ref(ref),
  ADD CONSTRAINT point_ledger_actor FOREIGN KEY(actor_id) REFERENCES users(id),
  ADD CONSTRAINT point_ledger_kind CHECK (kind IN ('EARN','REVERSAL','REDEEM','ADJUST')),
  ADD CONSTRAINT point_ledger_order CHECK (kind IN ('REDEEM','ADJUST') OR order_id IS NOT NULL);

-- Products: MERCH (cups, shirts) keeps stock; NULL stock = unlimited (food and drink). upgrade_to_id = the bigger combo.
ALTER TABLE products
  ADD COLUMN stock INT NULL,
  ADD COLUMN upgrade_to_id BIGINT NULL,
  ADD CONSTRAINT products_upgrade FOREIGN KEY(upgrade_to_id) REFERENCES products(id),
  ADD CONSTRAINT products_stock CHECK (stock IS NULL OR stock >= 0);

-- Gifts handed over at the counter (birthday gift of a tier, campaigns, Admin). One per (customer, campaign key).
CREATE TABLE gift_grants (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  reason VARCHAR(20) NOT NULL,
  campaign_key VARCHAR(120) NOT NULL,
  status VARCHAR(20) NOT NULL,
  issued_at DATETIME(6) NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  handed_at DATETIME(6) NULL,
  handed_by BIGINT NULL,
  cinema_id BIGINT NULL,
  note VARCHAR(500) NULL,
  actor_id BIGINT NULL,
  FOREIGN KEY(user_id) REFERENCES users(id),
  FOREIGN KEY(product_id) REFERENCES products(id),
  FOREIGN KEY(handed_by) REFERENCES users(id),
  FOREIGN KEY(cinema_id) REFERENCES cinemas(id),
  FOREIGN KEY(actor_id) REFERENCES users(id),
  UNIQUE KEY gift_grants_campaign(user_id, campaign_key),
  INDEX gift_grants_status(status, expires_at),
  CONSTRAINT gift_grants_status CHECK (status IN ('ISSUED','HANDED_OVER','REVOKED')),
  CONSTRAINT gift_grants_reason CHECK (reason IN ('BIRTHDAY','CAMPAIGN','ADMIN'))
);

-- Member-only showtimes (min_tier) and early sales: before public_sale_at only tiers with early_access_hours may book.
ALTER TABLE showtimes
  ADD COLUMN min_tier VARCHAR(20) NULL,
  ADD COLUMN public_sale_at DATETIME(6) NULL,
  ADD CONSTRAINT showtimes_min_tier FOREIGN KEY(min_tier) REFERENCES membership_tiers(code);

-- What each tier receives automatically.
ALTER TABLE membership_tiers
  ADD COLUMN birthday_voucher_id BIGINT NULL,
  ADD COLUMN birthday_vouchers INT NOT NULL DEFAULT 0,
  ADD COLUMN yearly_voucher_id BIGINT NULL,
  ADD COLUMN yearly_vouchers INT NOT NULL DEFAULT 0,
  ADD COLUMN next_voucher_id BIGINT NULL,
  ADD COLUMN birthday_gift_id BIGINT NULL,
  ADD COLUMN early_access_hours INT NOT NULL DEFAULT 0,
  ADD CONSTRAINT tiers_birthday_voucher FOREIGN KEY(birthday_voucher_id) REFERENCES vouchers(id),
  ADD CONSTRAINT tiers_yearly_voucher FOREIGN KEY(yearly_voucher_id) REFERENCES vouchers(id),
  ADD CONSTRAINT tiers_next_voucher FOREIGN KEY(next_voucher_id) REFERENCES vouchers(id),
  ADD CONSTRAINT tiers_birthday_gift FOREIGN KEY(birthday_gift_id) REFERENCES products(id),
  ADD CONSTRAINT tiers_counts CHECK (birthday_vouchers BETWEEN 0 AND 10 AND yearly_vouchers BETWEEN 0 AND 10 AND early_access_hours BETWEEN 0 AND 168);

-- Starter templates (Admin edits them under Kinh doanh → Voucher). Times are UTC.
INSERT INTO vouchers (code, description, kind, value, minimum, maximum, quota, per_user, starts_at, ends_at, active, audience, scope, min_tier, points_cost, valid_days) VALUES
('TV-SINHNHAT',      'Ưu đãi sinh nhật: giảm 10% đơn, tối đa 30.000đ',                       'PERCENT', 10,     0,      30000,  100000000, 1, '2026-01-01 00:00:00', '2099-12-31 00:00:00', 1, 'PERSONAL', 'ORDER',         NULL, 0,    31),
('TV-SINHNHAT-VANG', 'Voucher sinh nhật: giảm 50.000đ cho đơn từ 100.000đ',                  'FIXED',   50000,  100000, 0,      100000000, 1, '2026-01-01 00:00:00', '2099-12-31 00:00:00', 1, 'PERSONAL', 'ORDER',         NULL, 0,    31),
('TV-VE-MIENPHI',    '1 vé xem phim miễn phí (ghế thường hoặc VIP, tối đa 120.000đ)',         'FIXED',   1,      0,      120000, 100000000, 1, '2026-01-01 00:00:00', '2099-12-31 00:00:00', 1, 'PERSONAL', 'FREE_TICKET',   NULL, 0,    60),
('TV-NANGCAP-COMBO', 'Miễn phí nâng cấp 1 combo bắp nước lên cỡ lớn hơn',                     'FIXED',   1,      0,      0,      100000000, 1, '2026-01-01 00:00:00', '2099-12-31 00:00:00', 1, 'PERSONAL', 'COMBO_UPGRADE', NULL, 0,    90),
('TV-LANSAU',        'Giảm 5% cho lần mua vé tiếp theo, tối đa 20.000đ (đơn từ 100.000đ)',    'PERCENT', 5,      100000, 20000,  100000000, 1, '2026-01-01 00:00:00', '2099-12-31 00:00:00', 1, 'PERSONAL', 'ORDER',         NULL, 0,    30),
('DOI-20K',          'Đổi 200 điểm: giảm 20.000đ cho đơn bất kỳ',                              'FIXED',   20000,  0,      0,      100000000, 1, '2026-01-01 00:00:00', '2099-12-31 00:00:00', 1, 'PERSONAL', 'ORDER',         NULL, 200,  30),
('DOI-COMBO',        'Đổi 600 điểm: giảm 60.000đ tiền bắp nước',                               'FIXED',   60000,  0,      0,      100000000, 1, '2026-01-01 00:00:00', '2099-12-31 00:00:00', 1, 'PERSONAL', 'COMBO',         NULL, 600,  30),
('DOI-VE',           'Đổi 1.200 điểm: 1 vé xem phim miễn phí, tối đa 120.000đ',                'FIXED',   1,      0,      120000, 100000000, 1, '2026-01-01 00:00:00', '2099-12-31 00:00:00', 1, 'PERSONAL', 'FREE_TICKET',   NULL, 1200, 60);

-- Merchandise starts hidden with no stock; Admin sets price/stock/image and turns it on.
INSERT INTO products (name, kind, description, price, active, stock) VALUES
('Cốc CinePrime',      'MERCH', 'Cốc giữ nhiệt in logo CinePrime. Nhận tại quầy.',  89000,  0, 0),
('Áo thun CinePrime',  'MERCH', 'Áo thun cotton in logo CinePrime. Nhận tại quầy.', 199000, 0, 0);

UPDATE membership_tiers SET next_voucher_id = (SELECT id FROM vouchers WHERE code = 'TV-LANSAU');
UPDATE membership_tiers SET birthday_voucher_id = (SELECT id FROM vouchers WHERE code = 'TV-SINHNHAT'), birthday_vouchers = 1 WHERE code = 'SILVER';
UPDATE membership_tiers SET birthday_voucher_id = (SELECT id FROM vouchers WHERE code = 'TV-SINHNHAT-VANG'), birthday_vouchers = 1 WHERE code = 'GOLD';
UPDATE membership_tiers SET birthday_voucher_id = (SELECT id FROM vouchers WHERE code = 'TV-VE-MIENPHI'), birthday_vouchers = 1 WHERE code = 'PLATINUM';
UPDATE membership_tiers SET birthday_voucher_id = (SELECT id FROM vouchers WHERE code = 'TV-VE-MIENPHI'), birthday_vouchers = 2, early_access_hours = 24 WHERE code = 'DIAMOND';
UPDATE membership_tiers SET birthday_voucher_id = (SELECT id FROM vouchers WHERE code = 'TV-NANGCAP-COMBO'), birthday_vouchers = 2,
  yearly_voucher_id = (SELECT id FROM vouchers WHERE code = 'TV-VE-MIENPHI'), yearly_vouchers = 3, early_access_hours = 48,
  birthday_gift_id = (SELECT id FROM products WHERE name = 'Cốc CinePrime' AND kind = 'MERCH' ORDER BY id DESC LIMIT 1) WHERE code = 'VIP';
