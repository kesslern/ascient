ALTER TABLE users ADD COLUMN must_change_password BOOLEAN DEFAULT false;
UPDATE users SET must_change_password = true WHERE username = 'admin';