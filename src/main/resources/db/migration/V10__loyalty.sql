ALTER TABLE
    users ADD COLUMN cup_count INT NOT NULL DEFAULT 0;

ALTER TABLE
    orders ADD COLUMN discount_amount DECIMAL(
        5,
        2
    ) NOT NULL DEFAULT 0.00;
