-- users
ALTER TABLE
    users ADD COLUMN password_hash VARCHAR(60) NOT NULL DEFAULT '',
    ADD COLUMN user_role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER';

-- kiosk
ALTER TABLE
    kiosk_locations ADD COLUMN operating_status VARCHAR(20) NOT NULL DEFAULT 'OPEN';

ALTER TABLE
    kiosk_locations DROP
        COLUMN active;

-- products/menu items
ALTER TABLE
    menu_items ADD COLUMN description VARCHAR(500) NULL,
    ADD COLUMN category VARCHAR(30) NOT NULL DEFAULT 'HOT_DRINK';

UPDATE
    menu_items
SET
    category = 'WATER'
WHERE
    name = 'Mineral Water';

-- orders
ALTER TABLE
    orders ADD COLUMN total_cost DECIMAL(
        10,
        2
    ) NOT NULL DEFAULT 0.00;

-- order items
ALTER TABLE
    order_items ADD COLUMN customisation_note VARCHAR(255) NULL;
