-- ============================================================
--  SEED DATA  (loaded after Hibernate DDL via spring.sql.init)
-- ============================================================

-- ── Products ─────────────────────────────────────────────────
INSERT INTO products (name, description, price, stock, category, created_at, updated_at) VALUES
  ('Wireless Keyboard', 'Bluetooth mechanical keyboard with RGB backlighting', 89.99, 150, 'Electronics', NOW(), NOW()),
  ('Standing Desk Mat',  'Anti-fatigue mat for prolonged standing',            45.00, 200, 'Office',      NOW(), NOW()),
  ('USB-C Hub',          '7-in-1 hub with HDMI, SD card and USB 3.0 ports',   34.99, 300, 'Electronics', NOW(), NOW()),
  ('Notebook A5',        'Dotted hardcover notebook, 240 pages',               12.50, 500, 'Stationery',  NOW(), NOW()),
  ('Ergonomic Mouse',    'Vertical ergonomic mouse, reduces wrist strain',     55.00, 120, 'Electronics', NOW(), NOW());

-- ── Permissions ───────────────────────────────────────────────
-- Names must match the constants in security/Permissions.java
INSERT INTO app_permission (name, description) VALUES
  ('PRODUCT_READ',   'View product listings and details'),
  ('PRODUCT_WRITE',  'Create and update products'),
  ('PRODUCT_DELETE', 'Permanently delete products'),
  ('USER_READ',      'View user accounts and profiles'),
  ('USER_WRITE',     'Create, update and delete user accounts');

-- ── Roles ─────────────────────────────────────────────────────
INSERT INTO app_role (name, description) VALUES
  ('ROLE_ADMIN', 'Administrator — full access to all resources'),
  ('ROLE_USER',  'Standard user — read and write products only');

-- ── Role -> Permission mappings ───────────────────────────────
-- ADMIN gets every permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   app_role r, app_permission p
WHERE  r.name = 'ROLE_ADMIN';

-- USER gets product read + write (no delete, no user management)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   app_role r, app_permission p
WHERE  r.name = 'ROLE_USER'
AND    p.name IN ('PRODUCT_READ', 'PRODUCT_WRITE');

-- Users are seeded by DataInitializer.java at startup using
-- Spring's BCryptPasswordEncoder so passwords are always correctly hashed.