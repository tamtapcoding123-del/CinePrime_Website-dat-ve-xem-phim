-- Short text shown under the image on "Ưu đãi cho bạn" / "Góc điện ảnh" cards (17/11/2026). Empty = the card falls back to the
-- start of the article body.
ALTER TABLE contents ADD COLUMN summary VARCHAR(255) NULL;
