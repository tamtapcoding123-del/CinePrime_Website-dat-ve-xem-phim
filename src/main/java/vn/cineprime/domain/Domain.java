package vn.cineprime.domain;

import jakarta.persistence.*;
import java.time.*;

/** Persistent records use explicit foreign keys; services own aggregate transactions. */
public final class Domain {
    private Domain() {}
    @MappedSuperclass public static abstract class Row {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    }
    @Entity(name="Customer") @Table(name="users") public static class User extends Row {
        public String email, passwordHash, name, phone, avatar;
        public String role = "CUSTOMER";
        public Long cinemaId;
        public boolean enabled = true;
        public int authVersion;
        /** Cached membership tier; TierService moves it up at once and down only at the yearly review. */
        public String tierCode="SILVER"; public Instant tierSince; public Integer tierReviewYear;
        public LocalDate birthDate; public Instant birthDateSetAt;
    }
    @Entity @Table(name="membership_tiers") public static class Tier extends Row {
        public String code, name, color, benefits; public int rankOrder, ticketPct, comboPct; public long minSpend;
        /** Automatic benefits: vouchers issued in the birth month, each year, after each paid order; a gift; hours of early booking. */
        public Long birthdayVoucherId, yearlyVoucherId, nextVoucherId, birthdayGiftId; public int birthdayVouchers, yearlyVouchers, earlyAccessHours;
    }
    @Entity @Table(name="tier_history") public static class TierChange extends Row {
        public Long userId, actorId; public String fromCode, toCode, reason, note; public long spend; public Instant createdAt;
    }
    @Entity @Table(name="reset_tokens") public static class ResetToken extends Row {
        public Long userId; public String tokenHash; public Instant expiresAt; public boolean used;
    }
    @Entity @Table(name="genres") public static class Genre extends Row { public String name; public boolean active=true; }
    @Entity @Table(name="people") public static class Person extends Row { public String name, biography; public boolean active=true; }
    @Entity @Table(name="movies") public static class Movie extends Row {
        public String title, slug;
        @Column(length=6000) public String description;
        public String poster, banner, trailer, ageRating="P", status="COMING_SOON";
        public int duration=90; public LocalDate releaseDate, endDate; public boolean featured, active=true;
    }
    @Entity @Table(name="movie_genres") public static class MovieGenre extends Row { public Long movieId, genreId; }
    @Entity @Table(name="credits") public static class Credit extends Row { public Long movieId, personId; public String kind="ACTOR"; }
    @Entity @Table(name="cinemas") public static class Cinema extends Row {
        public String name, city, address, phone;
        @Column(length=6000) public String description;
        public boolean active=true;
    }
    @Entity @Table(name="rooms") public static class Room extends Row { public Long cinemaId; public String name, format="2D"; public boolean active=true; }
    @Entity @Table(name="seats") public static class Seat extends Row {
        public Long roomId; public String label, rowLabel, type="STANDARD", coupleGroup;
        public int position; public boolean active=true;
    }
    @Entity @Table(name="showtimes") public static class Showtime extends Row {
        public Long movieId, roomId; public Instant startsAt, endsAt;
        public String language="Phụ đề", format="2D", status="DRAFT";
        public long surcharge;
        /** Member-only showtime (lowest tier allowed) and the public sales start; before it only tiers with early access may book. */
        public String minTier; public Instant publicSaleAt;
    }
    @Entity @Table(name="prices") public static class Price extends Row {
        public Long cinemaId; public String seatType="STANDARD", dayType="WEEKDAY";
        public int fromMinute, toMinute=1440; public long amount;
    }
    @Entity @Table(name="products") public static class Product extends Row {
        public String name, kind="COMBO", image, description; public long price; public boolean active=true;
        /** MERCH keeps stock (null = unlimited); upgradeToId = the bigger combo a COMBO_UPGRADE voucher moves to. */
        public Integer stock; public Long upgradeToId;
    }
    @Entity @Table(name="vouchers") public static class Voucher extends Row {
        public String code, description, kind="FIXED"; public long value, minimum, maximum;
        public int quota=100, perUser=1; public Instant startsAt, endsAt; public boolean active=true;
        /** PUBLIC typed code or PERSONAL wallet template; scope ORDER/TICKET/COMBO/FREE_TICKET/COMBO_UPGRADE. */
        public String audience="PUBLIC", scope="ORDER", minTier; public int pointsCost, validDays=30;
    }
    @Entity(name="BookingOrder") @Table(name="orders") public static class Order extends Row {
        public String code; public Long userId, showtimeId, voucherId; public Long userVoucherId;
        public String status="PENDING_PAYMENT";
        public Instant createdAt, expiresAt, paidAt;
        /** discount = tierDiscount + voucher discount; tier percentages are snapshotted when the order is priced. */
        public long subtotal, discount, total, tierDiscount; public boolean frozen; public String tierCode; public int tierTicketPct, tierComboPct;
        public String movieTitle, cinemaName, roomName; public Instant showStartsAt;
        /** COUNTER orders are sold by Staff for cash; userId is null for walk-in customers. */
        public String channel="ONLINE"; public Long soldBy, shiftId, cancelledBy; public Instant cancelledAt; public String cancelReason;
    }
    @Entity @Table(name="inventory") public static class Inventory extends Row {
        public Long showtimeId, seatId, orderId; public String status="AVAILABLE"; public Instant expiresAt;
    }
    @Entity @Table(name="order_seats") public static class OrderSeat extends Row {
        public Long orderId, seatId; public String label, seatType; public long price, discount;
    }
    @Entity @Table(name="order_products") public static class OrderProduct extends Row {
        public Long orderId, productId; public String name; public int quantity; public long price, discount;
    }
    @Entity @Table(name="voucher_uses") public static class VoucherUse extends Row {
        public Long voucherId, orderId, userId; public String status="RESERVED"; public Instant expiresAt;
    }
    @Entity @Table(name="payments") public static class Payment extends Row {
        public Long orderId; public String provider="MOMO", requestId, externalOrderId, transactionId;
        public String status="CREATED";
        @Column(length=2048) public String payUrl;
        public long amount;
        public Integer resultCode; public boolean reconciliation; public Instant createdAt, updatedAt;
        public Long received, shiftId;
    }
    @Entity @Table(name="tickets") public static class Ticket extends Row {
        public Long orderId, seatId, showtimeId; public String code, token, status="VALID";
        public Instant checkedInAt; public Long checkedInBy;
    }
    @Entity @Table(name="point_ledger") public static class Point extends Row { public Long userId, orderId, actorId; public long points; public String kind="EARN", ref, note; public Instant createdAt; }
    /** A voucher copy in one customer's wallet; usable while ISSUED and before expiresAt, held by at most one pending order. */
    @Entity @Table(name="user_vouchers") public static class UserVoucher extends Row {
        public Long userId, voucherId, orderId, actorId; public String code, source, campaignKey, status="ISSUED"; public Instant issuedAt, expiresAt, reservedUntil, usedAt;
    }
    @Entity @Table(name="gift_grants") public static class GiftGrant extends Row {
        public Long userId, productId, handedBy, cinemaId, actorId; public String reason, campaignKey, status="ISSUED", note; public Instant issuedAt, expiresAt, handedAt;
    }
    @Entity @Table(name="favorites") public static class Favorite extends Row { public Long userId, movieId; }
    @Entity @Table(name="reviews") public static class Review extends Row {
        public Long userId, movieId; public int stars;
        @Column(length=2000) public String comment;
        public boolean visible=true; public Instant createdAt;
    }
    @Entity @Table(name="subscriptions") public static class Subscription extends Row { public Long userId, movieId; public boolean email=true, sent; }
    @Entity @Table(name="notifications") public static class Notification extends Row {
        public Long userId; public String subject;
        @Column(length=6000) public String body;
        public boolean seen; public Instant createdAt;
    }
    @Entity @Table(name="contents") public static class Content extends Row {
        public String kind="ARTICLE", title, slug, image, link, summary;
        @Column(length=20000) public String body;
        public boolean active=true; public Instant publishedAt;
    }
    /** Home hero slide: MOVIE points at a movie (own image optional), PROMO is a standalone image with an internal link. */
    @Entity @Table(name="banners") public static class Banner extends Row {
        public String kind="PROMO", title, image, link; public Long movieId;
        public int sortOrder; public boolean active=true; public Instant startsAt, endsAt;
    }
    @Entity @Table(name="settings") public static class Setting extends Row { public String name, value; }
    /** Single row: website maintenance window edited by Admin (see MaintenanceService for how it is read). */
    @Entity @Table(name="maintenance") public static class Maintenance extends Row {
        public boolean enabled; public String title, message, affected, staffMode="CHECKIN", contactPhone, contactEmail;
        public Instant startsAt, expectedEndAt, updatedAt; public Long updatedBy;
    }
    @Entity @Table(name="audit_logs") public static class Audit extends Row {
        public Long actorId; public String action, target; public Instant createdAt;
    }
    /** Cash drawer session of one Staff member at one cinema; expected cash is snapshotted when closing. */
    @Entity @Table(name="shifts") public static class Shift extends Row {
        public Long staffId, cinemaId; public String status="OPEN"; public Instant openedAt, closedAt;
        public long openingCash; public Long expectedCash, countedCash; public String note;
    }
    @Entity @Table(name="mail_jobs") public static class MailJob extends Row {
        public Long userId; public String dedupKey, destination, subject;
        @Column(length=12000) public String body;
        public int attempts; public Instant nextAttemptAt, sentAt;
    }
}
