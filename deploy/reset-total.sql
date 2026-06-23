-- FastBuy — Total Reset (production Neon Postgres)
--
-- Use this to start the event over (e.g. a bartender is stuck, or you want a
-- clean slate for a new demo run). Run it in the Neon dashboard -> SQL Editor.
--
-- It does three things, atomically:
--   1. Deletes ALL orders and resets order IDs so the next order is FB1 again.
--   2. Restores every product's stock to its original seed value.
--   3. Re-anchors the demo event dates to "now" so the lineup makes sense and
--      classifies dynamically (live / upcoming / finished) against today.
--
-- Bars, products, categories and any linked Mercado Pago payment accounts are
-- left untouched. Customers/bartenders mid-session will see their orders
-- disappear on the next poll — that's the intended reset.
--
-- Why step 3: prod runs on a persistent DB, so the one-time seeded event dates
-- froze at provisioning time (and the "live" ones were seeded perpetual). The
-- backend already hides events ended >6h ago and the UI badges live/upcoming/
-- finished by the clock — they just need fresh anchors. Re-run this before a
-- demo day to bring the lineup back to "today".

BEGIN;

-- 1) Wipe orders + restart the FB id counter at 1.
TRUNCATE TABLE orders RESTART IDENTITY;

-- 2) Restore product stock to seed values (must match deploy/postgres-init.sql).
UPDATE products AS p
SET stock = v.stock
FROM (VALUES
    ('p1',  50),  ('p2',  45),  ('p3',  60),  ('p4',  40),  ('p5',  100),
    ('p6',  80),  ('p7',  75),  ('p8',  30),  ('p9',  35),  ('p10', 20),
    ('p11', 15),  ('p12', 25),  ('p13', 30),  ('p14', 10),  ('p15', 12),
    ('p16', 60),  ('p17', 40),  ('p18', 50),  ('p19', 30),  ('p20', 100),
    ('p21', 20),  ('p22', 25),  ('p23', 15)
) AS v(id, stock)
WHERE p.id = v.id;

-- 3) Re-anchor event dates relative to NOW() (mirrors the dev seed windows).
--    e1/e2 run from earlier today into tonight (LIVE); e3 is tomorrow; e4 ended
--    well past the 6h grace so it stays hidden; e5 is far-future (upcoming).
UPDATE events SET starts_at = NOW() - INTERVAL '6 hours',  ends_at = NOW() + INTERVAL '12 hours' WHERE id = 'e1';
UPDATE events SET starts_at = NOW() - INTERVAL '3 hours',  ends_at = NOW() + INTERVAL '9 hours'  WHERE id = 'e2';
UPDATE events SET starts_at = NOW() + INTERVAL '1 day',    ends_at = NOW() + INTERVAL '2 days'   WHERE id = 'e3';
UPDATE events SET starts_at = NOW() - INTERVAL '10 hours', ends_at = NOW() - INTERVAL '8 hours'  WHERE id = 'e4';
UPDATE events SET starts_at = NOW() + INTERVAL '300 days', ends_at = NOW() + INTERVAL '303 days' WHERE id = 'e5';

COMMIT;

-- Quick sanity check (optional):
--   SELECT count(*) AS orders_left FROM orders;          -- expect 0
--   SELECT id, stock FROM products ORDER BY id;          -- expect seed values
--   SELECT id, starts_at, ends_at FROM events ORDER BY starts_at;  -- anchored to today
