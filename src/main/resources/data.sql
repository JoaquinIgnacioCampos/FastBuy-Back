-- Categories
INSERT INTO categories (id, label, emoji) VALUES ('drink', 'Bebidas', '🍺');
INSERT INTO categories (id, label, emoji) VALUES ('snack', 'Snacks',  '🍟');
INSERT INTO categories (id, label, emoji) VALUES ('food',  'Comida',  '🌭');
INSERT INTO categories (id, label, emoji) VALUES ('all',   'Todos',   '🍽️');

-- Events (relative timestamps so the data stays live across a full dev session).
INSERT INTO events (id, name, venue, hours, starts_at, ends_at) VALUES
    ('e1',  'Festival Eclipse',            'Costanera Sur · CABA',         '21:00 - 04:00', DATEADD('HOUR', -6,  CURRENT_TIMESTAMP), DATEADD('HOUR', 12,  CURRENT_TIMESTAMP));
INSERT INTO events (id, name, venue, hours, starts_at, ends_at) VALUES
    ('e2',  'Festival Cumbiero',           'Parque Sarmiento · CABA',      '20:00 - 03:00', DATEADD('HOUR', -3,  CURRENT_TIMESTAMP), DATEADD('HOUR', 9,   CURRENT_TIMESTAMP));
INSERT INTO events (id, name, venue, hours, starts_at, ends_at) VALUES
    ('e3',  'Cosquín Rock',                'Cosquín · Córdoba',            '18:00 - 02:00', DATEADD('DAY',  1,   CURRENT_TIMESTAMP), DATEADD('DAY',  2,   CURRENT_TIMESTAMP));
INSERT INTO events (id, name, venue, hours, starts_at, ends_at) VALUES
    ('e5',  'Lollapalooza Argentina 2027', 'Hipódromo de Palermo',         '14:00 - 23:00', DATEADD('DAY',  300, CURRENT_TIMESTAMP), DATEADD('DAY',  303, CURRENT_TIMESTAMP));

-- Bars for Eclipse (e1) — 3 bars, full festival setup
INSERT INTO bars (id, label, location, event_id) VALUES ('eclipse-north',  'Barra Norte',    'Sector Norte · Entrada Principal',        'e1');
INSERT INTO bars (id, label, location, event_id) VALUES ('eclipse-center', 'Barra Central',  'Centro del Predio · cerca del escenario', 'e1');
INSERT INTO bars (id, label, location, event_id) VALUES ('eclipse-south',  'Barra Sur VIP',  'Sector Sur · Zona VIP',                   'e1');

-- Bars for Cumbiero (e2) — 2 bars, smaller event
INSERT INTO bars (id, label, location, event_id) VALUES ('cumbia-main', 'Barra Principal', 'Patio Central · Frente al Escenario', 'e2');
INSERT INTO bars (id, label, location, event_id) VALUES ('cumbia-vip',  'Barra VIP',       'Sector Dorado · Acceso Exclusivo',    'e2');

-- Eclipse products (p1–p15): standard beer-heavy festival menu
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p1',  'Carlsberg 500ml',       'drink', 8000,  50, '🍺', '🍺', 'Lata · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p2',  'Heineken 500ml',        'drink', 8500,  45, '🍻', '🍻', 'Lata · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p3',  'Corona Extra 355ml',    'drink', 7000,  60, '🌴', '🌴', 'Porrón · 355 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p4',  'Stella Artois 500ml',   'drink', 9000,  40, '🌟', '🌟', 'Pinta · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p5',  'Agua mineral 500ml',    'drink', 2500, 100, '💧', '💧', 'Botella · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p6',  'Coca-Cola 500ml',       'drink', 3000,  80, '🥤', '🥤', 'Lata · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p7',  'Sprite 500ml',          'drink', 3000,  75, '🍋', '🍋', 'Lata · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p8',  'Nachos con salsa',      'snack', 5000,  30, '🌮', '🌮', 'Porción individual');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p9',  'Papas fritas',          'snack', 4500,  35, '🍟', '🍟', 'Porción individual');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p10', 'Mini pizza',            'food',  7500,  20, '🍕', '🍕', 'Muzzarella · 4 porciones');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p11', 'Chorizo a la parrilla', 'food',  9000,  15, '🌭', '🌭', 'Pan + chimichurri');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p12', 'Panchos con cheddar',   'food',  6500,  25, '🧀', '🧀', 'Doble cheddar');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p13', 'Empanadas x3',          'food',  8000,  30, '🥟', '🥟', 'Surtido · 3 unidades');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p14', 'Champagne Premium',     'drink', 18000, 10, '🍾', '🍾', 'Copa · sólo VIP');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p15', 'Whisky Premium',        'drink', 15000, 12, '🥃', '🥃', 'Vaso · sólo VIP');

-- Cumbiero products (p16–p23): wine/fernet-heavy — distinct vibe
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p16', 'Fernet con Coca',       'drink', 5500,  60, '🫗', '🫗', 'Vaso largo · 400 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p17', 'Malbec Copa',           'drink', 6500,  40, '🍷', '🍷', 'Copa · 200 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p18', 'Sangría 500ml',         'drink', 5000,  50, '🍹', '🍹', 'Vaso · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p19', 'IPA Artesanal 500ml',   'drink', 9500,  30, '🫙', '🫙', 'Lata · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p20', 'Agua mineral 500ml',    'drink', 2500, 100, '💧', '💧', 'Botella · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p21', 'Provoleta a la parrilla','food', 10500, 20, '🧆', '🧆', 'Con aceite y orégano');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p22', 'Choripán',              'food',  9000,  25, '🥖', '🥖', 'Pan brioche + chimichurri');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p23', 'Tabla de fiambres',     'snack', 14000, 15, '🪵', '🪵', '2 personas · surtida');

-- MP test product (dev-only, served at all bars)
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p_test', 'Test MP $5',  'drink', 5, 999, '🧪', '🧪', 'Dev only · real MP charge');

-- Eclipse bar ↔ products
-- eclipse-north: standard drinks + most food/snacks
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p1');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p2');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p3');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p5');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p6');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p7');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p8');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p9');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p11');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p12');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p13');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-north', 'p_test');

-- eclipse-center: premium beers + most food
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-center', 'p1');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-center', 'p2');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-center', 'p4');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-center', 'p5');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-center', 'p6');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-center', 'p8');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-center', 'p9');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-center', 'p10');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-center', 'p12');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-center', 'p13');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-center', 'p_test');

-- eclipse-south (VIP): premium drinks + VIP exclusives
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-south', 'p2');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-south', 'p4');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-south', 'p5');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-south', 'p6');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-south', 'p10');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-south', 'p13');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-south', 'p14');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-south', 'p15');
INSERT INTO bar_products (bar_id, product_id) VALUES ('eclipse-south', 'p_test');

-- Cumbiero bar ↔ products
-- cumbia-main: fernet, wine, sangría + food
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-main', 'p16');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-main', 'p17');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-main', 'p18');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-main', 'p20');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-main', 'p21');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-main', 'p22');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-main', 'p_test');

-- cumbia-vip: artisan beer + premium + food tabla
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-vip', 'p16');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-vip', 'p17');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-vip', 'p19');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-vip', 'p20');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-vip', 'p21');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-vip', 'p22');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-vip', 'p23');
INSERT INTO bar_products (bar_id, product_id) VALUES ('cumbia-vip', 'p_test');

-- Sample orders
-- eclipse-north: 4 QUEUE orders to demonstrate oldest-first lock + disabled buttons
INSERT INTO orders (total, items, status, bar,            time) VALUES (16500, '[{"pid":"p1","q":2},{"pid":"p11","q":1}]',  'QUEUE',     'eclipse-north',  '2:14');
INSERT INTO orders (total, items, status, bar,            time) VALUES (9000,  '[{"pid":"p4","q":1}]',                      'PREPARING', 'eclipse-north',  '2:22');
INSERT INTO orders (total, items, status, bar,            time) VALUES (8000,  '[{"pid":"p3","q":2}]',                      'QUEUE',     'eclipse-north',  '2:31');
INSERT INTO orders (total, items, status, bar,            time) VALUES (4500,  '[{"pid":"p7","q":1}]',                      'QUEUE',     'eclipse-north',  '2:38');
INSERT INTO orders (total, items, status, bar,            time) VALUES (13000, '[{"pid":"p5","q":1},{"pid":"p6","q":1}]',   'QUEUE',     'eclipse-north',  '2:44');
-- eclipse-center / eclipse-south
INSERT INTO orders (total, items, status, bar,            time) VALUES (5000,  '[{"pid":"p8","q":1}]',                      'READY',     'eclipse-center', '2:30');
INSERT INTO orders (total, items, status, bar,            time) VALUES (12500, '[{"pid":"p2","q":1},{"pid":"p10","q":1}]',  'QUEUE',     'eclipse-center', '2:45');
INSERT INTO orders (total, items, status, bar,            time) VALUES (36000, '[{"pid":"p14","q":2}]',                     'QUEUE',     'eclipse-south',  '2:18');
INSERT INTO orders (total, items, status, bar,            time) VALUES (23500, '[{"pid":"p15","q":1},{"pid":"p4","q":1}]',  'PREPARING', 'eclipse-south',  '2:35');
-- cumbia bars
INSERT INTO orders (total, items, status, bar,            time) VALUES (11000, '[{"pid":"p16","q":2}]',                     'QUEUE',     'cumbia-main',    '3:10');
INSERT INTO orders (total, items, status, bar,            time) VALUES (6500,  '[{"pid":"p17","q":1}]',                     'PREPARING', 'cumbia-vip',     '3:15');
