-- FastBuy — Total Reset (production Neon Postgres)
--
-- Use this to start the event over (e.g. a bartender is stuck, or you want a
-- clean slate for a new demo run). Run it in the Neon dashboard -> SQL Editor.
--
-- It does two things, atomically:
--   1. Deletes ALL orders and resets order IDs so the next order is FB1 again.
--   2. Restores every product's stock to its original seed value.
--
-- Catalog (products/bars/events/categories) and any linked Mercado Pago
-- payment accounts are left untouched. Customers/bartenders mid-session will
-- see their orders disappear on the next poll — that's the intended reset.

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

COMMIT;

-- Quick sanity check (optional):
--   SELECT count(*) AS orders_left FROM orders;          -- expect 0
--   SELECT id, stock FROM products ORDER BY id;          -- expect seed values
