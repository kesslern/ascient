CREATE TABLE users (
  id              SERIAL PRIMARY KEY,
  username        VARCHAR(36) NOT NULL UNIQUE,
  password        CHAR(70) NOT NULL
);

INSERT INTO users (username, password) VALUES ('admin', '$2a$12$v40jGVGr2uqgX9C7EQAGKOuk95TQbyYitYmLJFE1gdIsLHVaBa7m.')
