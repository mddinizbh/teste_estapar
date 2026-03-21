-- Optimistic locking: coluna version em TODAS as entidades
ALTER TABLE spot ADD COLUMN version INT NOT NULL DEFAULT 0;
ALTER TABLE parking_session ADD COLUMN version INT NOT NULL DEFAULT 0;
ALTER TABLE sector ADD COLUMN version INT NOT NULL DEFAULT 0;

-- Índice para findActiveByPlate (query mais frequente)
CREATE INDEX idx_session_plate_status ON parking_session(license_plate, status);

-- Índice para revenue query
CREATE INDEX idx_session_sector_exit ON parking_session(sector_id, exit_time);

-- Unique constraint coordenadas de spot
ALTER TABLE spot ADD CONSTRAINT uq_spot_coordinates UNIQUE (lat, lng);
