CREATE
    TABLE
        orders(
            id BIGINT NOT NULL AUTO_INCREMENT,
            customer_name VARCHAR(120) NOT NULL,
            customer_email VARCHAR(254) NOT NULL,
            pickup_time DATETIME NOT NULL,
            status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- status values: PENDING | ACCEPTED | IN_PROGRESS | READY | COLLECTED | CANCELLED
            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY(id)
        );

CREATE
    TABLE
        order_items(
            id BIGINT NOT NULL AUTO_INCREMENT,
            order_id BIGINT NOT NULL,
            menu_item_id BIGINT NOT NULL,
            SIZE VARCHAR(10) NOT NULL, -- REGULAR or LARGE
            quantity INT NOT NULL, -- snapshot of price at order time
            unit_price DECIMAL(
                5,
                2
            ) NOT NULL,
            PRIMARY KEY(id),
            CONSTRAINT fk_order_items_order FOREIGN KEY(order_id) REFERENCES orders(id),
            CONSTRAINT fk_order_items_menu_item FOREIGN KEY(menu_item_id) REFERENCES menu_items(id)
        );
