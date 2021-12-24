CREATE TABLE IF NOT EXISTS device (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    gateway_id uuid NOT NULL,
    peripheral_address integer NOT NULL,
    peripheral_index integer NOT NULL,
    type character varying(128) NOT NULL,
    name character varying(128) NOT NULL,
    last_seen timestamp without time zone NOT NULL,
    CONSTRAINT fk__device__gateway_id FOREIGN KEY(gateway_id) REFERENCES gateway(id)
);
