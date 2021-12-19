CREATE TABLE IF NOT EXISTS device (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id uuid NOT NULL,
    device_type character varying(128) NOT NULL,
    device_name character varying(128) NOT NULL,
    traits character varying(128)[] NOT NULL,
    last_state character varying(128) NOT NULL,
    CONSTRAINT fk__device__account_id FOREIGN KEY(account_id) REFERENCES account(id)
);
