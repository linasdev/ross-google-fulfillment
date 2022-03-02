ALTER TABLE device
    ADD COLUMN IF NOT EXISTS synced_to_homegraph boolean NOT NULL DEFAULT false;
