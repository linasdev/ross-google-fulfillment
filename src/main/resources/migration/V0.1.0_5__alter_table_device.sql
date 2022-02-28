ALTER TABLE device
    DROP CONSTRAINT IF EXISTS uq__device__gateway_id_peripheral_address_peripheral_index_type;
ALTER TABLE device
    ADD CONSTRAINT uq__device__gateway_id_peripheral_address_peripheral_index_type UNIQUE(gateway_id, peripheral_address, peripheral_index, type);
