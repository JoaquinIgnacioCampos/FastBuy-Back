-- Categories
INSERT INTO categories (id, label, emoji) VALUES ('drink', 'Bebidas', '🍺');
INSERT INTO categories (id, label, emoji) VALUES ('snack', 'Snacks', '🍟');
INSERT INTO categories (id, label, emoji) VALUES ('food', 'Comida', '🌭');
INSERT INTO categories (id, label, emoji) VALUES ('all', 'Todos', '🍽️');

-- Bars
INSERT INTO bars (id, label, location) VALUES ('north', 'Barra Norte', 'Sector Norte - Entrada Principal');
INSERT INTO bars (id, label, location) VALUES ('center', 'Barra Central', 'Centro del Predio');
INSERT INTO bars (id, label, location) VALUES ('south', 'Barra Sur', 'Sector Sur - Zona VIP');

-- Products
INSERT INTO products (id, name, category, price, stock) VALUES ('p1',  'Carlsberg 500ml',       'drink', 8000, 50);
INSERT INTO products (id, name, category, price, stock) VALUES ('p2',  'Heineken 500ml',         'drink', 8500, 45);
INSERT INTO products (id, name, category, price, stock) VALUES ('p3',  'Corona Extra 355ml',     'drink', 7000, 60);
INSERT INTO products (id, name, category, price, stock) VALUES ('p4',  'Stella Artois 500ml',    'drink', 9000, 40);
INSERT INTO products (id, name, category, price, stock) VALUES ('p5',  'Agua mineral 500ml',     'drink', 2500, 100);
INSERT INTO products (id, name, category, price, stock) VALUES ('p6',  'Coca-Cola 500ml',        'drink', 3000, 80);
INSERT INTO products (id, name, category, price, stock) VALUES ('p7',  'Sprite 500ml',           'drink', 3000, 75);
INSERT INTO products (id, name, category, price, stock) VALUES ('p8',  'Nachos con salsa',       'snack', 5000, 30);
INSERT INTO products (id, name, category, price, stock) VALUES ('p9',  'Papas fritas',           'snack', 4500, 35);
INSERT INTO products (id, name, category, price, stock) VALUES ('p10', 'Mini pizza',             'food',  7500, 20);
INSERT INTO products (id, name, category, price, stock) VALUES ('p11', 'Chorizo a la parrilla',  'food',  9000, 15);
INSERT INTO products (id, name, category, price, stock) VALUES ('p12', 'Panchos con cheddar',    'food',  6500, 25);
INSERT INTO products (id, name, category, price, stock) VALUES ('p13', 'Empanadas x3',           'food',  8000, 30);

-- Events
INSERT INTO events (id, name, venue, hours) VALUES ('e1', 'Lollapalooza Argentina 2025', 'Hipódromo de Palermo', '14:00 - 23:00');

-- Sample orders for bartender screen testing
INSERT INTO orders (total, items, status, bar, time) VALUES (16500, '[{"pid":"p1","q":2},{"pid":"p11","q":1}]', 'QUEUE',     'north',  '2:14');
INSERT INTO orders (total, items, status, bar, time) VALUES (9000,  '[{"pid":"p4","q":1}]',                    'PREPARING', 'north',  '2:22');
INSERT INTO orders (total, items, status, bar, time) VALUES (5000,  '[{"pid":"p8","q":1}]',                    'READY',     'center', '2:30');
