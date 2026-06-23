-- FastBuy — production DB migration for the 2026-06-23 release.
--
-- Run ONCE in the Neon dashboard -> SQL Editor on the existing production
-- database, BEFORE/ALONGSIDE deploying the new backend.
--
-- Why: queue position / ETA are now computed from a denormalized per-order
-- beverage count instead of parsing each order's items JSON. Prod runs with
-- ddl-auto=none, so the column must be added by hand (fresh installs get it
-- from postgres-init.sql automatically).
--
-- Without this, creating and reading orders fails (INSERT/SELECT reference a
-- missing column). Safe to run anytime (idempotent); existing rows default to 0.

ALTER TABLE orders ADD COLUMN IF NOT EXISTS item_count INT NOT NULL DEFAULT 0;
