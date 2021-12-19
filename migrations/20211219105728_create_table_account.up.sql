CREATE TABLE IF NOT EXISTS account (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    token_subject character varying(128) NOT NULL,
    CONSTRAINT uq__account__token_subject UNIQUE(token_subject)
);
