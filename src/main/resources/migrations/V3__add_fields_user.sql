CREATE TABLE cars (
    id INT NOT NULL UNIQUE,
    make VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    color VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    userId INT,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES users(id)
);