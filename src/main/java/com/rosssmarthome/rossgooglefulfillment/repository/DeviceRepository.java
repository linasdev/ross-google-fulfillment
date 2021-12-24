package com.rosssmarthome.rossgooglefulfillment.repository;

import com.rosssmarthome.rossgooglefulfillment.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Device d WHERE d.gateway.id = :gatewayId AND d.peripheralAddress = :peripheralAddress AND d.peripheralIndex = :peripheralIndex")
    Device loadByGatewayIdAndPeripheralDetails(UUID gatewayId, Long peripheralAddress, Long peripheralIndex);
}
