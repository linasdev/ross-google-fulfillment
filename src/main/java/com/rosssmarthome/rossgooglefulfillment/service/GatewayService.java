package com.rosssmarthome.rossgooglefulfillment.service;

import com.rosssmarthome.rossgooglefulfillment.data.DeviceState;
import com.rosssmarthome.rossgooglefulfillment.data.GatewayState;
import com.rosssmarthome.rossgooglefulfillment.data.StateKey;
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
public class GatewayService {
    private final DeviceService deviceService;

    @Transactional
    public void handleNewState(UUID gatewayId, GatewayState request) {
        for (DeviceState deviceState : request.getDeviceStates()) {
            Device device = deviceService.loadByGatewayIdAndPeripheralDetails(
                    gatewayId,
                    deviceState.getPeripheralAddress(),
                    deviceState.getPeripheralIndex()
            );

            if (device == null) {
                log.warn(
                        "Cannot find device by gateway id ({}), peripheral address ({}) and peripheral index ({})",
                        gatewayId,
                        deviceState.getPeripheralAddress(),
                        deviceState.getPeripheralIndex()
                );
                continue;
            }

            device.clearStates();
            deviceState.getPeripheralStates().forEach((key, value) -> {
                String actualValue = value;

                if (key == StateKey.BRIGHTNESS) {
                    actualValue = String.valueOf(Long.valueOf(actualValue) / 100L);
                }

                device.addState(
                        State.builder()
                                .key(key)
                                .value(value)
                                .build()
                );
            });

            deviceService.save(device);
        }
    }
}
