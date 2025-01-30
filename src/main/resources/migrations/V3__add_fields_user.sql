CREATE TABLE cars (
    id INT NOT NULL UNIQUE,
    model VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    color VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    user_id INT,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);