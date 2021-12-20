CREATE TABLE IF NOT EXISTS gateway (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id uuid NOT NULL,
    CONSTRAINT fk__gateway__account_id FOREIGN KEY (account_id) REFERENCES account(id)
);
