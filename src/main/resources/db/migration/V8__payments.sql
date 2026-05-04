CREATE
    TABLE
        payments(
            id BIGINT NOT NULL AUTO_INCREMENT,
            order_id BIGINT NOT NULL,
            transaction_reference VARCHAR(64) NULL,
            payment_status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
            payment_method VARCHAR(10) NOT NULL,
            processed_at DATETIME NULL,
            PRIMARY KEY(id),
            UNIQUE KEY uk_payments_order(order_id),
            CONSTRAINT fk_payments_order FOREIGN KEY(order_id) REFERENCES orders(id)
        );
