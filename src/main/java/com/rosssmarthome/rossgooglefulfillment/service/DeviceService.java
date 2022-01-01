package com.rosssmarthome.rossgooglefulfillment.service;

import com.rosssmarthome.rossgooglefulfillment.data.DeviceType;
import com.rosssmarthome.rossgooglefulfillment.entity.Device;
import com.rosssmarthome.rossgooglefulfillment.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;

    @Transactional(readOnly = true)
    public Device getById(UUID deviceId) {
        return deviceRepository.getById(deviceId);
    }

    @Transactional(readOnly = true)
    public Device getByGatewayIdAndPeripheralDetailsAndType(UUID gatewayId, Long peripheralAddress, Long peripheralIndex, DeviceType type) {
        return deviceRepository.getByGatewayIdAndPeripheralAddressAndPeripheralIndexAndType(gatewayId, peripheralAddress, peripheralIndex, type);
    }

    @Transactional
    public void save(Device device) {
        deviceRepository.save(device);
    }
}
