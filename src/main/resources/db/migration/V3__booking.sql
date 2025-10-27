CREATE TABLE bookings (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          room_id BIGINT NOT NULL,
                          request_id VARCHAR(255) NOT NULL,
                          start_date DATE NOT NULL,
                          end_date DATE NOT NULL,
                          booking_type VARCHAR(20) NOT NULL, -- 'TEMPORARY' or 'PERMANENT'
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (room_id) REFERENCES rooms(id),
                          CONSTRAINT unique_request_id UNIQUE (request_id),
                          CONSTRAINT check_booking_type CHECK (booking_type IN ('TEMPORARY', 'PERMANENT'))
);