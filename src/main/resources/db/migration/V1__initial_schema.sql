-- ============================================================
-- V1__initial_schema.sql
-- Creates all tables for the Flight Booking System.
-- ============================================================

-- ── Users ────────────────────────────────────────────────────
CREATE TABLE users (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    first_name                  VARCHAR(50)     NOT NULL,
    last_name                   VARCHAR(50)     NOT NULL,
    email                       VARCHAR(100)    NOT NULL UNIQUE,
    password                    VARCHAR(255)    NOT NULL,
    phone_number                VARCHAR(20),
    role                        VARCHAR(20)     NOT NULL DEFAULT 'CUSTOMER',
    is_enabled                  BOOLEAN         NOT NULL DEFAULT TRUE,
    is_account_locked           BOOLEAN         NOT NULL DEFAULT FALSE,
    email_verified              BOOLEAN         NOT NULL DEFAULT FALSE,
    verification_token          VARCHAR(255),
    password_reset_token        VARCHAR(255),
    password_reset_token_expiry DATETIME,
    created_at                  DATETIME        NOT NULL,
    updated_at                  DATETIME,
    PRIMARY KEY (id),
    INDEX idx_users_email (email)
);

-- ── Flights ──────────────────────────────────────────────────
CREATE TABLE flights (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    flight_number       VARCHAR(10)     NOT NULL UNIQUE,
    airline             VARCHAR(100)    NOT NULL,
    departure_airport   VARCHAR(3)      NOT NULL,
    arrival_airport     VARCHAR(3)      NOT NULL,
    departure_city      VARCHAR(100)    NOT NULL,
    arrival_city        VARCHAR(100)    NOT NULL,
    departure_time      DATETIME        NOT NULL,
    arrival_time        DATETIME        NOT NULL,
    base_price          DECIMAL(10,2)   NOT NULL,
    total_seats         INT             NOT NULL,
    available_seats     INT             NOT NULL,
    flight_class        VARCHAR(20)     NOT NULL DEFAULT 'ECONOMY',
    status              VARCHAR(20)     NOT NULL DEFAULT 'SCHEDULED',
    gate                VARCHAR(10),
    terminal            VARCHAR(10),
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME,
    PRIMARY KEY (id),
    INDEX idx_flights_number    (flight_number),
    INDEX idx_flights_departure (departure_airport, departure_time),
    INDEX idx_flights_arrival   (arrival_airport)
);

-- ── Bookings ─────────────────────────────────────────────────
CREATE TABLE bookings (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    booking_reference   VARCHAR(10)     NOT NULL UNIQUE,
    user_id             BIGINT          NOT NULL,
    flight_id           BIGINT          NOT NULL,
    number_of_seats     INT             NOT NULL,
    total_price         DECIMAL(10,2)   NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    passenger_names     TEXT,
    contact_email       VARCHAR(100),
    contact_phone       VARCHAR(20),
    special_requests    TEXT,
    cancellation_reason TEXT,
    cancelled_at        DATETIME,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_bookings_reference (booking_reference),
    INDEX idx_bookings_user             (user_id),
    INDEX idx_bookings_flight           (flight_id),
    CONSTRAINT fk_bookings_user   FOREIGN KEY (user_id)   REFERENCES users   (id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_flight FOREIGN KEY (flight_id) REFERENCES flights (id) ON DELETE RESTRICT
);

-- ── Payments ─────────────────────────────────────────────────
CREATE TABLE payments (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id              BIGINT          NOT NULL UNIQUE,
    transaction_id          VARCHAR(100),
    amount                  DECIMAL(10,2)   NOT NULL,
    currency                VARCHAR(3)      NOT NULL DEFAULT 'USD',
    payment_method          VARCHAR(30)     NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    gateway_response        TEXT,
    failure_reason          VARCHAR(255),
    refund_amount           DECIMAL(10,2),
    refund_transaction_id   VARCHAR(100),
    refunded_at             DATETIME,
    paid_at                 DATETIME,
    created_at              DATETIME        NOT NULL,
    updated_at              DATETIME,
    PRIMARY KEY (id),
    INDEX idx_payments_transaction (transaction_id),
    INDEX idx_payments_booking     (booking_id),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
);
