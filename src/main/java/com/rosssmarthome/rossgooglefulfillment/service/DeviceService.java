package com.rosssmarthome.rossgooglefulfillment.service;

import com.rosssmarthome.rossgooglefulfillment.data.StateUpdateRequest;
import com.rosssmarthome.rossgooglefulfillment.entity.Device;
import com.rosssmarthome.rossgooglefulfillment.entity.State;
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

    @Transactional
    public void handleStateUpdate(UUID deviceId, StateUpdateRequest request) {
        Device device = deviceRepository.findById(deviceId).orElse(null);

        if (device == null) {
            log.warn("Cannot find device with id: {}", deviceId);
            return;
        }

        device.clearStates();
        request.getStates().forEach((key, value) -> device.addState(
                State.builder()
                    .key(key)
                    .value(value)
                    .build())
        );

        deviceRepository.save(device);
    }
}
