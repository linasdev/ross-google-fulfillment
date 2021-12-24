CREATE TABLE IF NOT EXISTS state (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_id uuid NOT NULL,
    state_key character varying(128) NOT NULL,
    state_value character varying(128) NOT NULL,
    CONSTRAINT fk__gateway__device_id FOREIGN KEY (device_id) REFERENCES device(id)
);
