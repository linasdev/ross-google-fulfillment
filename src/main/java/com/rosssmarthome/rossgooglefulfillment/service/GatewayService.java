package com.rosssmarthome.rossgooglefulfillment.service;

import com.google.api.services.homegraph.v1.HomeGraphService;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationDevice;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationRequest;
import com.google.api.services.homegraph.v1.model.RequestSyncDevicesRequest;
import com.google.api.services.homegraph.v1.model.StateAndNotificationPayload;
import com.rosssmarthome.rossgooglefulfillment.data.DeviceState;
import com.rosssmarthome.rossgooglefulfillment.data.GatewayState;
import com.rosssmarthome.rossgooglefulfillment.entity.Device;
import com.rosssmarthome.rossgooglefulfillment.entity.Gateway;
import com.rosssmarthome.rossgooglefulfillment.repository.GatewayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayService {
    private final GatewayRepository gatewayRepository;
    private final DeviceService deviceService;
    private final HomeGraphService homeGraphService;

    @Transactional
    public void handleNewState(UUID gatewayId, GatewayState request) {
        Gateway gateway = gatewayRepository.getById(gatewayId);

        if (gateway == null) {
            log.warn("Gateway with id ({}) not found", gatewayId);
            return;
        }

        HashMap<String, Object> states = new HashMap<>();

        boolean requestSync = false;

        for (DeviceState state : request.getDeviceStates()) {
            Device device = deviceService.getByGatewayIdAndPeripheralDetailsAndType(gatewayId, state.getPeripheralAddress(), state.getPeripheralIndex(), state.getPeripheralState().getType());

            if (device == null) {
                log.info("Device with gateway id ({}), peripheral address ({}) and peripheral index ({}) not found", gatewayId, state.getPeripheralAddress(), state.getPeripheralIndex());

                device = Device.builder()
                        .gateway(gateway)
                        .name(state.getPeripheralState().getType().getGoogleDeviceName())
                        .type(state.getPeripheralState().getType())
                        .peripheralAddress(state.getPeripheralAddress())
                        .peripheralIndex(state.getPeripheralIndex())
                        .lastSeen(LocalDateTime.now())
                        .build();

                requestSync = true;
            } else {
                device.setLastSeen(LocalDateTime.now());
            }

            deviceService.save(device);

            states.put(device.getId().toString(), state.getPeripheralState().getGoogleDeviceState());
        }

        if (requestSync) {
            log.info("Requesting HomeGraph sync");

            RequestSyncDevicesRequest content = new RequestSyncDevicesRequest();
            content.setAgentUserId(gateway.getAccount().getId().toString());

            try {
                homeGraphService.devices().requestSync(content).execute();
            } catch (IOException e) {
                log.warn("Failed to request HomeGraph sync", e);
            }
        }

        UUID requestId = UUID.randomUUID();

        log.info("Sending HomeGraph report state and notification request with id: {}", requestId);

        ReportStateAndNotificationDevice devices = new ReportStateAndNotificationDevice();
        devices.setStates(states);

        StateAndNotificationPayload payload = new StateAndNotificationPayload();
        payload.setDevices(devices);

        ReportStateAndNotificationRequest content = new ReportStateAndNotificationRequest();
        content.setRequestId(requestId.toString());
        content.setAgentUserId(gateway.getAccount().getId().toString());
        content.setPayload(payload);

        try {
            homeGraphService.devices().reportStateAndNotification(content).execute();
        } catch (IOException e) {
            log.warn("Failed to report device state to HomeGraph", e);
        }
    }
}
