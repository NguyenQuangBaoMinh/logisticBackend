CREATE DATABASE IF NOT EXISTS supply_chain;
USE supply_chain;

CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

-- Insert sample user (username: admin, password: admin123)
INSERT INTO user (username, password, role, active) 
VALUES ('admin', 'admin123', 'ROLE_ADMIN', true)
ON DUPLICATE KEY UPDATE username = username; 

