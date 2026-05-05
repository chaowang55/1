INSERT
    INTO
        users(
            full_name,
            email,
            password_hash,
            user_role
        )
    VALUES(
        'ADMIN',
        'admin@whistlestop.com',
        '$2a$10$IzU2Em4j2BXU23AoXgMkPOxMyjb7cNden0dI.foGp0.w/Us.5Fe0y',
        'STAFF'
    );

-- password: admin123
