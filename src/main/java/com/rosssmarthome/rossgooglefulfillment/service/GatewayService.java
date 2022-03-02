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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        boolean requestHomegraphSync = false;

        List<Device> devices = new ArrayList<>();

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
                        .syncedToHomegraph(false)
                        .lastSeen(LocalDateTime.now())
                        .build();

                requestHomegraphSync = true;
            } else {
                device.setLastSeen(LocalDateTime.now());

                if (!device.getSyncedToHomegraph()) {
                    requestHomegraphSync = true;
                }
            }

            deviceService.save(device);

            states.put(device.getId().toString(), state.getPeripheralState().getGoogleDeviceState());
            devices.add(device);
        }

        if (requestHomegraphSync) {
            log.info("Requesting HomeGraph sync");

            RequestSyncDevicesRequest content = new RequestSyncDevicesRequest();
            content.setAgentUserId(gateway.getAccount().getId().toString());

            try {
                homeGraphService.devices().requestSync(content).execute();
                devices.forEach(device -> {
                    device.setSyncedToHomegraph(true);
                    deviceService.save(device);
                });
            } catch (IOException e) {
                log.warn("Failed to request HomeGraph sync", e);
            }
        }

        UUID requestId = UUID.randomUUID();

        log.info("Sending HomeGraph report state and notification request with id: {}", requestId);

        ReportStateAndNotificationDevice reportStateRequestDevices = new ReportStateAndNotificationDevice();
        reportStateRequestDevices.setStates(states);

        StateAndNotificationPayload reportStateRequestPayload = new StateAndNotificationPayload();
        reportStateRequestPayload.setDevices(reportStateRequestDevices);

        ReportStateAndNotificationRequest reportStateRequest = new ReportStateAndNotificationRequest();
        reportStateRequest.setRequestId(requestId.toString());
        reportStateRequest.setAgentUserId(gateway.getAccount().getId().toString());
        reportStateRequest.setPayload(reportStateRequestPayload);

        try {
            homeGraphService.devices().reportStateAndNotification(reportStateRequest).execute();
        } catch (IOException e) {
            log.warn("Failed to report device state to HomeGraph", e);
        }
    }
}
