-- ============================================================
-- V2__seed_data.sql
-- Inserts a default admin user and sample flights.
-- Admin password: Admin@1234  (BCrypt hash below)
-- ============================================================

-- Default admin account
INSERT INTO users (first_name, last_name, email, password, role, is_enabled, email_verified, created_at)
VALUES ('System', 'Admin',
        'admin@flightbooking.com',
        '$2a$12$RbNGDnVMuNT1ySS8j5cRfOBhkXHEFX/E37YMVhEOPWq3tNwFyb9qi',
        'ADMIN', TRUE, TRUE, NOW());

-- Sample flights (JFK → LHR)
INSERT INTO flights (flight_number, airline, departure_airport, arrival_airport, departure_city, arrival_city,
                     departure_time, arrival_time, base_price, total_seats, available_seats,
                     flight_class, status, gate, terminal, created_at)
VALUES
('BA0117', 'British Airways', 'JFK', 'LHR', 'New York', 'London',
 DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 + 1 DAY),
 450.00, 200, 200, 'ECONOMY', 'SCHEDULED', 'B12', 'T7', NOW()),

('AA0100', 'American Airlines', 'JFK', 'LHR', 'New York', 'London',
 DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 + 1 DAY),
 520.00, 180, 180, 'ECONOMY', 'SCHEDULED', 'A3', 'T8', NOW()),

('VS0003', 'Virgin Atlantic', 'JFK', 'LHR', 'New York', 'London',
 DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 + 1 DAY),
 680.00, 50, 50, 'BUSINESS', 'SCHEDULED', 'C5', 'T4', NOW()),

-- LAX → NRT
('JL0061', 'Japan Airlines', 'LAX', 'NRT', 'Los Angeles', 'Tokyo',
 DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 10 + 1 DAY),
 780.00, 250, 250, 'ECONOMY', 'SCHEDULED', 'D1', 'TBIT', NOW()),

-- LHR → DXB
('EK0003', 'Emirates', 'LHR', 'DXB', 'London', 'Dubai',
 DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 + 0 DAY) + INTERVAL 7 HOUR,
 350.00, 400, 400, 'ECONOMY', 'SCHEDULED', 'E10', 'T3', NOW());
