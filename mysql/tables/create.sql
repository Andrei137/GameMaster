CREATE TABLE USER
(
    user_id INT(5),
    username VARCHAR(256) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    email VARCHAR(256),
    is_banned BOOLEAN DEFAULT FALSE,
    type VARCHAR(16) NOT NULL,

    PRIMARY KEY (user_id),
    CHECK (type IN ('client', 'provider', 'admin'))
);

CREATE TABLE CLIENT
(
    user_id INT(5),
    first_name VARCHAR(256) NOT NULL,
    last_name VARCHAR(256) NOT NULL,
    phone_number VARCHAR(15),

    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
);

CREATE TABLE PROVIDER
(
    user_id INT(5),
    website VARCHAR(256),
    type_provider VARCHAR(16) NOT NULL,

    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    CHECK (type_provider IN ('developer', 'publisher'))
);

CREATE TABLE ADMIN
(
    user_id INT(5),
    cut_percentage DECIMAL(10, 2) NOT NULL,

    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    CHECK (cut_percentage >= 0 AND cut_percentage <= 100)
);

CREATE TABLE GAME
(
    game_id INT(5),
    developer_id INT(5),
    publisher_id INT(5),
    name VARCHAR(128) NOT NULL UNIQUE,
    price DECIMAL(10, 2),
    release_date DATE,
    is_visible BOOLEAN DEFAULT TRUE,
    type VARCHAR(16) NOT NULL,

    PRIMARY KEY (game_id),
    FOREIGN KEY (developer_id) REFERENCES PROVIDER(user_id),
    FOREIGN KEY (publisher_id) REFERENCES PROVIDER(user_id),
    CHECK (price >= 0),
    CHECK (type IN ('game', 'dlc'))
);

CREATE TABLE DLC
(
    game_id INT(5),
    base_game_id INT(5),

    PRIMARY KEY (game_id),
    FOREIGN KEY (game_id) REFERENCES GAME(game_id),
    FOREIGN KEY (base_game_id) REFERENCES GAME(game_id)
);

CREATE TABLE PURCHASE
(
    game_id INT(5),
    user_id INT(5),
    purchase_date DATE,
    price DECIMAL(10, 2) NOT NULL,

    PRIMARY KEY (game_id, user_id),
    FOREIGN KEY (game_id) REFERENCES GAME(game_id),
    FOREIGN KEY (user_id) REFERENCES CLIENT(user_id),
    CHECK (price >= 0)
);

CREATE TABLE WISHLIST
(
    game_id INT(5),
    user_id INT(5),
    added_date DATE,

    PRIMARY KEY (user_id, game_id),
    FOREIGN KEY (game_id) REFERENCES GAME(game_id),
    FOREIGN KEY (user_id) REFERENCES CLIENT(user_id)
);

CREATE TABLE CONTRACT
(
    developer_id INT(5),
    publisher_id INT(5),
    status VARCHAR(16) NOT NULL,
    start_date DATE,
    end_date DATE,

    PRIMARY KEY (developer_id, publisher_id),
    FOREIGN KEY (developer_id) REFERENCES PROVIDER(user_id),
    FOREIGN KEY (publisher_id) REFERENCES PROVIDER(user_id),
    CHECK (status IN ('accepted', 'pending')),
    CHECK (start_date <= end_date)
);