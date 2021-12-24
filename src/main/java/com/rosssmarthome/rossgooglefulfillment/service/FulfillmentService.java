package com.rosssmarthome.rossgooglefulfillment.service;

import com.google.actions.api.smarthome.*;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.home.graph.v1.DeviceProto;
import com.rosssmarthome.rossgooglefulfillment.entity.Account;
import com.rosssmarthome.rossgooglefulfillment.entity.Device;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FulfillmentService extends SmartHomeApp {
    private final AccountService accountService;
    private final CloudIot cloudIot;

    @NotNull
    @Override
    public SyncResponse onSync(SyncRequest request, Map<?, ?> headers) {
        Account account = accountService.findOrCreate(getTokenSubject());

        List<SyncResponse.Payload.Device> devices = account.getGateways()
                .stream()
                .flatMap(gateway -> gateway.getDevices().stream())
                .map(device -> new SyncResponse.Payload.Device.Builder()
                        .setId(device.getId().toString())
                        .setType(device.getType().getGoogleDeviceType())
                        .setName(DeviceProto.DeviceNames.newBuilder()
                                .setName(device.getName())
                                .build())
                        .setTraits(device.getType().getGoogleDeviceTraits())
                        .build())
                .collect(Collectors.toList());

        SyncResponse.Payload payload = new SyncResponse.Payload(account.getId().toString(), devices.toArray(new SyncResponse.Payload.Device[0]));
        SyncResponse response = new SyncResponse(request.getRequestId(), payload);

        return response;
    }

    @NotNull
    @Override
    public QueryResponse onQuery(QueryRequest request, Map<?, ?> headers) {
        Account account = accountService.findOrCreate(getTokenSubject());

        QueryRequest.Inputs.Payload.Device[] queryDevices = ((QueryRequest.Inputs) request.getInputs()[0]).getPayload().getDevices();

        Map<String, Map<String, Object>> responseDevices = new HashMap<>();

        for (QueryRequest.Inputs.Payload.Device queryDevice : queryDevices) {
            Device device = account.findDeviceById(UUID.fromString(queryDevice.getId()));

            HashMap<String, Object> responseDevice = new HashMap<>();

            if (device != null) {
                responseDevice.put("status", "SUCCESS");
                responseDevice.put("online", true);
                responseDevice.putAll(device.getGoogleDeviceState());
            } else {
                responseDevice.put("status", "ERROR");
                responseDevice.put("errorCode", "deviceOffline");
            }

            responseDevices.put(device.getId().toString(), responseDevice);
        }

        QueryResponse.Payload payload = new QueryResponse.Payload(responseDevices);
        QueryResponse response = new QueryResponse(request.getRequestId(), payload);

        return response;
    }

    @NotNull
    @Override
    public ExecuteResponse onExecute(ExecuteRequest request, Map<?, ?> headers) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public void onDisconnect(DisconnectRequest request, Map<?, ?> headers) {
        throw new UnsupportedOperationException();
    }

    private String getTokenSubject() {
        Jwt token = (Jwt) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        return token.getSubject();
    }
}
