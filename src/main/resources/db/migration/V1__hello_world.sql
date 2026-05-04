CREATE
    TABLE
        users(
            id BIGINT NOT NULL AUTO_INCREMENT,
            full_name VARCHAR(120) NOT NULL, -- https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address
            email VARCHAR(254) NOT NULL,
            PRIMARY KEY(id),
            UNIQUE KEY uk_users_email(email)
        );
