CREATE TABLE classroom (
    id INT NOT NULL UNIQUE,
    specialization VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    max_students INT,
    PRIMARY KEY (id)
);

CREATE TABLE users_classroom (
  user_id INT,
  class_id INT,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (class_id) REFERENCES classroom(id)
);