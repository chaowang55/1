CREATE
    TABLE
        opening_hours(
            day_of_week INT NOT NULL,
            open_time TIME NULL,
            close_time TIME NULL,
            PRIMARY KEY(day_of_week)
        );

-- 1=Monday ... 7=Sunday. NULL times mean closed.
INSERT
    INTO
        opening_hours(
            day_of_week,
            open_time,
            close_time
        )
    VALUES(
        1,
        '06:30:00',
        '19:00:00'
    ),
    (
        2,
        '06:30:00',
        '19:00:00'
    ),
    (
        3,
        '06:30:00',
        '19:00:00'
    ),
    (
        4,
        '06:30:00',
        '19:00:00'
    ),
    (
        5,
        '06:30:00',
        '19:00:00'
    ),
    (
        6,
        '07:00:00',
        '18:00:00'
    ),
    (
        7,
        NULL,
        NULL
    );
