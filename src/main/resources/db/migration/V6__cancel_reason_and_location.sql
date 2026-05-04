CREATE
    TABLE
        kiosk_locations(
            id BIGINT NOT NULL AUTO_INCREMENT,
            name VARCHAR(120) NOT NULL,
            station_name VARCHAR(120) NOT NULL,
            active BOOLEAN NOT NULL DEFAULT TRUE,
            PRIMARY KEY(id)
        );

INSERT
    INTO
        kiosk_locations(
            name,
            station_name,
            active
        )
    VALUES(
        'Whistlestop Coffee Hut',
        'Cramlington Station',
        TRUE
    );

ALTER TABLE
    orders ADD COLUMN kiosk_location_id BIGINT NULL;

ALTER TABLE
    orders ADD COLUMN cancellation_reason VARCHAR(255) NULL;

UPDATE
    orders
SET
    kiosk_location_id = 1
WHERE
    kiosk_location_id IS NULL;

ALTER TABLE
    orders ADD CONSTRAINT fk_orders_kiosk_location FOREIGN KEY(kiosk_location_id) REFERENCES kiosk_locations(id);
