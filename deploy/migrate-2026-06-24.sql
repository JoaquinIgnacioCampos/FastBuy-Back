-- FastBuy — production DB migration for the 2026-06-24 release.
--
-- Run ONCE in the Neon dashboard -> SQL Editor on the existing production
-- database, BEFORE/ALONGSIDE deploying the new backend.
--
-- Why: adds the admin_users table for per-event admin authentication.
-- Admins are seeded by DataInitializer on first boot (BCrypt hashes computed at
-- runtime), so no INSERT rows are needed here.
-- Fresh installs get the table from postgres-init.sql automatically.

CREATE TABLE IF NOT EXISTS admin_users (
    id            VARCHAR(50)  NOT NULL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    event_id      VARCHAR(50)  NOT NULL,
    session_token VARCHAR(100),
    FOREIGN KEY (event_id) REFERENCES events(id)
);
