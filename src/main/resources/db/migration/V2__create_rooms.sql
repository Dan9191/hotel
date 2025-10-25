CREATE TABLE rooms (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       hotel_id BIGINT NOT NULL,
                       number VARCHAR(50) NOT NULL,
                       available BOOLEAN NOT NULL DEFAULT TRUE,
                       times_booked BIGINT NOT NULL DEFAULT 0,
                       FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);