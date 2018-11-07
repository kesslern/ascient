CREATE TABLE booleans (
  id              SERIAL PRIMARY KEY,
  name            VARCHAR(25) NOT NULL UNIQUE,
  value           BOOLEAN NOT NULL
);
