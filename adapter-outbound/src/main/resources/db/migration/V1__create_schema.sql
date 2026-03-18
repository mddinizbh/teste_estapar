CREATE TABLE sector (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE,
    base_price DECIMAL(10,2) NOT NULL,
    max_capacity INT NOT NULL
);

CREATE TABLE spot (
    id BIGINT PRIMARY KEY,
    sector_id BIGINT NOT NULL,
    lat DOUBLE NOT NULL,
    lng DOUBLE NOT NULL,
    occupied BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (sector_id) REFERENCES sector(id)
);

CREATE TABLE parking_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    sector_id BIGINT NOT NULL,
    spot_id BIGINT,
    entry_time TIMESTAMP NOT NULL,
    parked_time TIMESTAMP NULL,
    exit_time TIMESTAMP NULL,
    price_at_entry DECIMAL(10,2) NOT NULL,
    amount_charged DECIMAL(10,2),
    status VARCHAR(10) NOT NULL,
    FOREIGN KEY (sector_id) REFERENCES sector(id),
    FOREIGN KEY (spot_id) REFERENCES spot(id)
);
