ALTER TABLE parking_session
ADD COLUMN active_license_plate VARCHAR(20) GENERATED ALWAYS AS (
    CASE WHEN exit_time IS NULL THEN license_plate ELSE NULL END
) STORED;

ALTER TABLE parking_session
ADD CONSTRAINT uq_active_session UNIQUE (active_license_plate);
