-- Categories
INSERT INTO categories (id, label, emoji) VALUES ('drink', 'Bebidas', '🍺');
INSERT INTO categories (id, label, emoji) VALUES ('snack', 'Snacks', '🍟');
INSERT INTO categories (id, label, emoji) VALUES ('food',  'Comida', '🌭');
INSERT INTO categories (id, label, emoji) VALUES ('all',   'Todos',  '🍽️');

-- Bars
INSERT INTO bars (id, label, location) VALUES ('north',  'Barra Norte',   'Sector Norte · Entrada Principal');
INSERT INTO bars (id, label, location) VALUES ('center', 'Barra Central', 'Centro del Predio · cerca del escenario');
INSERT INTO bars (id, label, location) VALUES ('south',  'Barra Sur',     'Sector Sur · Zona VIP');

-- Products. Each product has a unique glyph in `image` so the menu reads as a
-- visually distinct grid; `emoji` is a fallback used by the ProductImage renderer.
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p1',  'Carlsberg 500ml',        'drink', 8000, 50,  '🍺', '🍺', 'Lata · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p2',  'Heineken 500ml',         'drink', 8500, 45,  '🍻', '🍻', 'Lata · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p3',  'Corona Extra 355ml',     'drink', 7000, 60,  '🌴', '🌴', 'Porrón · 355 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p4',  'Stella Artois 500ml',    'drink', 9000, 40,  '🌟', '🌟', 'Pinta · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p5',  'Agua mineral 500ml',     'drink', 2500, 100, '💧', '💧', 'Botella · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p6',  'Coca-Cola 500ml',        'drink', 3000, 80,  '🥤', '🥤', 'Lata · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p7',  'Sprite 500ml',           'drink', 3000, 75,  '🍋', '🍋', 'Lata · 500 ml');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p8',  'Nachos con salsa',       'snack', 5000, 30,  '🌮', '🌮', 'Porción individual');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p9',  'Papas fritas',           'snack', 4500, 35,  '🍟', '🍟', 'Porción individual');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p10', 'Mini pizza',             'food',  7500, 20,  '🍕', '🍕', 'Muzzarella · 4 porciones');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p11', 'Chorizo a la parrilla',  'food',  9000, 15,  '🌭', '🌭', 'Pan + chimichurri');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p12', 'Panchos con cheddar',    'food',  6500, 25,  '🧀', '🧀', 'Doble cheddar');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p13', 'Empanadas x3',           'food',  8000, 30,  '🥟', '🥟', 'Surtido · 3 unidades');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p14', 'Champagne Premium',      'drink', 18000, 10, '🍾', '🍾', 'Copa · sólo VIP');
INSERT INTO products (id, name, category, price, stock, image, emoji, subtitle) VALUES ('p15', 'Whisky Premium',         'drink', 15000, 12, '🥃', '🥃', 'Vaso · sólo VIP');

-- Bar ↔ Products (each bar serves a subset; some products are everywhere, some are bar-exclusive)
-- north: stocks the standard drinks + most food/snacks
INSERT INTO bar_products (bar_id, product_id) VALUES ('north',  'p1');
INSERT INTO bar_products (bar_id, product_id) VALUES ('north',  'p2');
INSERT INTO bar_products (bar_id, product_id) VALUES ('north',  'p3');
INSERT INTO bar_products (bar_id, product_id) VALUES ('north',  'p5');
INSERT INTO bar_products (bar_id, product_id) VALUES ('north',  'p6');
INSERT INTO bar_products (bar_id, product_id) VALUES ('north',  'p7');
INSERT INTO bar_products (bar_id, product_id) VALUES ('north',  'p8');
INSERT INTO bar_products (bar_id, product_id) VALUES ('north',  'p9');
INSERT INTO bar_products (bar_id, product_id) VALUES ('north',  'p11');
INSERT INTO bar_products (bar_id, product_id) VALUES ('north',  'p12');
INSERT INTO bar_products (bar_id, product_id) VALUES ('north',  'p13');

-- center: stocks the standard drinks + premium beers + most food
INSERT INTO bar_products (bar_id, product_id) VALUES ('center', 'p1');
INSERT INTO bar_products (bar_id, product_id) VALUES ('center', 'p2');
INSERT INTO bar_products (bar_id, product_id) VALUES ('center', 'p4');
INSERT INTO bar_products (bar_id, product_id) VALUES ('center', 'p5');
INSERT INTO bar_products (bar_id, product_id) VALUES ('center', 'p6');
INSERT INTO bar_products (bar_id, product_id) VALUES ('center', 'p8');
INSERT INTO bar_products (bar_id, product_id) VALUES ('center', 'p9');
INSERT INTO bar_products (bar_id, product_id) VALUES ('center', 'p10');
INSERT INTO bar_products (bar_id, product_id) VALUES ('center', 'p12');
INSERT INTO bar_products (bar_id, product_id) VALUES ('center', 'p13');

-- south (VIP): stocks premium drinks + small food selection + VIP exclusives
INSERT INTO bar_products (bar_id, product_id) VALUES ('south',  'p2');
INSERT INTO bar_products (bar_id, product_id) VALUES ('south',  'p4');
INSERT INTO bar_products (bar_id, product_id) VALUES ('south',  'p5');
INSERT INTO bar_products (bar_id, product_id) VALUES ('south',  'p6');
INSERT INTO bar_products (bar_id, product_id) VALUES ('south',  'p10');
INSERT INTO bar_products (bar_id, product_id) VALUES ('south',  'p13');
INSERT INTO bar_products (bar_id, product_id) VALUES ('south',  'p14');  -- VIP exclusive
INSERT INTO bar_products (bar_id, product_id) VALUES ('south',  'p15');  -- VIP exclusive

-- Events (relative timestamps so the data updates with every run).
-- Live windows are deliberately wide (~half a day) so they stay live across a
-- full dev session without re-seeding.
INSERT INTO events (id, name, venue, hours, starts_at, ends_at) VALUES
    ('e1', 'Festival Eclipse',            'Costanera Sur · CABA',         '21:00 - 04:00', DATEADD('HOUR', -6,  CURRENT_TIMESTAMP), DATEADD('HOUR', 12,  CURRENT_TIMESTAMP));
INSERT INTO events (id, name, venue, hours, starts_at, ends_at) VALUES
    ('e1b','Festival Cumbiero',           'Parque Sarmiento · CABA',      '20:00 - 03:00', DATEADD('HOUR', -3,  CURRENT_TIMESTAMP), DATEADD('HOUR', 9,   CURRENT_TIMESTAMP));
INSERT INTO events (id, name, venue, hours, starts_at, ends_at) VALUES
    ('e1c','Trasnoche Indie',             'Club Niceto · Palermo',        '22:00 - 06:00', DATEADD('HOUR', -2,  CURRENT_TIMESTAMP), DATEADD('HOUR', 8,   CURRENT_TIMESTAMP));
INSERT INTO events (id, name, venue, hours, starts_at, ends_at) VALUES
    ('e2', 'Cosquín Rock',                'Cosquín · Córdoba',            '18:00 - 02:00', DATEADD('DAY',  1,   CURRENT_TIMESTAMP), DATEADD('DAY',  2,   CURRENT_TIMESTAMP));
INSERT INTO events (id, name, venue, hours, starts_at, ends_at) VALUES
    ('e3', 'Quilmes Rock',                'Tecnópolis · Buenos Aires',    '17:00 - 23:00', DATEADD('HOUR', -10, CURRENT_TIMESTAMP), DATEADD('HOUR', -2,  CURRENT_TIMESTAMP));
INSERT INTO events (id, name, venue, hours, starts_at, ends_at) VALUES
    ('e4', 'Lollapalooza Argentina 2025', 'Hipódromo de Palermo',         '14:00 - 23:00', DATEADD('DAY',  -60, CURRENT_TIMESTAMP), DATEADD('DAY',  -57, CURRENT_TIMESTAMP));
INSERT INTO events (id, name, venue, hours, starts_at, ends_at) VALUES
    ('e5', 'Lollapalooza Argentina 2027', 'Hipódromo de Palermo',         '14:00 - 23:00', DATEADD('DAY',  300, CURRENT_TIMESTAMP), DATEADD('DAY',  303, CURRENT_TIMESTAMP));

-- Sample orders distributed across all three bars and statuses
INSERT INTO orders (total, items, status, bar,    time) VALUES (16500, '[{"pid":"p1","q":2},{"pid":"p11","q":1}]',                'QUEUE',     'north',  '2:14');
INSERT INTO orders (total, items, status, bar,    time) VALUES (9000,  '[{"pid":"p4","q":1}]',                                    'PREPARING', 'north',  '2:22');
INSERT INTO orders (total, items, status, bar,    time) VALUES (5000,  '[{"pid":"p8","q":1}]',                                    'READY',     'center', '2:30');
INSERT INTO orders (total, items, status, bar,    time) VALUES (12500, '[{"pid":"p2","q":1},{"pid":"p10","q":1}]',                'QUEUE',     'center', '2:45');
INSERT INTO orders (total, items, status, bar,    time) VALUES (7500,  '[{"pid":"p9","q":1},{"pid":"p6","q":1}]',                 'PREPARING', 'center', '2:50');
INSERT INTO orders (total, items, status, bar,    time) VALUES (36000, '[{"pid":"p14","q":2}]',                                   'QUEUE',     'south',  '2:18');
INSERT INTO orders (total, items, status, bar,    time) VALUES (23500, '[{"pid":"p15","q":1},{"pid":"p4","q":1}]',                'PREPARING', 'south',  '2:35');
INSERT INTO orders (total, items, status, bar,    time) VALUES (18000, '[{"pid":"p14","q":1}]',                                   'READY',     'south',  '2:55');
