-- FastBuy — production DB migration for the 2026-06-22 release.
--
-- Run ONCE in the Neon dashboard -> SQL Editor on the existing production
-- database, BEFORE/ALONGSIDE deploying the new backend.
--
-- Why: the bartender-write auth gate stores a per-login session token on
-- bartender_users. Prod runs with ddl-auto=none, so the column must be added
-- by hand (fresh installs get it from postgres-init.sql automatically).
--
-- Without this, bartender login fails (UPDATE references a missing column).

ALTER TABLE bartender_users ADD COLUMN IF NOT EXISTS session_token VARCHAR(100);
