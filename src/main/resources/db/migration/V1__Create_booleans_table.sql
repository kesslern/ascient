CREATE TABLE booleans (
  id              SERIAL PRIMARY KEY,
  name            VARCHAR(36) NOT NULL UNIQUE,
  value           BOOLEAN NOT NULL
);
