CREATE
    TABLE
        menu_items(
            id BIGINT NOT NULL AUTO_INCREMENT,
            name VARCHAR(120) NOT NULL, -- NULL means the item has no size variants (e.g. Mineral Water)
            regular_price DECIMAL(
                5,
                2
            ) NULL,
            large_price DECIMAL(
                5,
                2
            ) NULL,
            available BOOLEAN NOT NULL DEFAULT TRUE,
            PRIMARY KEY(id)
        );

-- Seed the initial menu
INSERT
    INTO
        menu_items(
            name,
            regular_price,
            large_price
        )
    VALUES(
        'Americano',
        1.50,
        2.00
    ),
    (
        'Americano with Milk',
        2.00,
        2.50
    ),
    (
        'Latte',
        2.50,
        3.00
    ),
    (
        'Cappuccino',
        2.50,
        3.00
    ),
    (
        'Hot Chocolate',
        2.00,
        2.50
    ),
    (
        'Mocha',
        2.50,
        3.00
    ),
    (
        'Mineral Water',
        1.00,
        NULL
    );
