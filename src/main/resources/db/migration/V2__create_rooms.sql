CREATE TABLE rooms (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       hotel_id BIGINT NOT NULL,
                       number VARCHAR(255) NOT NULL,
                       FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);