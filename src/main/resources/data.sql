-- This file is loaded automatically by Spring Boot on startup
-- because spring.sql.init.mode=always in application.properties

INSERT INTO products (name, description, price, stock, category, created_at, updated_at) VALUES
  ('Wireless Keyboard', 'Bluetooth mechanical keyboard with RGB backlighting', 89.99, 150, 'Electronics', NOW(), NOW()),
  ('Standing Desk Mat', 'Anti-fatigue mat for prolonged standing', 45.00, 200, 'Office', NOW(), NOW()),
  ('USB-C Hub', '7-in-1 USB-C hub with HDMI, SD card, and USB 3.0 ports', 34.99, 300, 'Electronics', NOW(), NOW()),
  ('Notebook A5', 'Dotted hardcover notebook, 240 pages', 12.50, 500, 'Stationery', NOW(), NOW()),
  ('Ergonomic Mouse', 'Vertical ergonomic mouse, reduces wrist strain', 55.00, 120, 'Electronics', NOW(), NOW());
