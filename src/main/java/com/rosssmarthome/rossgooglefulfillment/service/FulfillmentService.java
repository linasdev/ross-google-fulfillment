package com.rosssmarthome.rossgooglefulfillment.service;

import com.google.actions.api.smarthome.*;
import com.google.home.graph.v1.DeviceProto;
import com.rosssmarthome.rossgooglefulfillment.entity.Account;
import com.rosssmarthome.rossgooglefulfillment.entity.Device;
import com.rosssmarthome.rossgooglefulfillment.entity.State;
import com.rosssmarthome.rossgooglefulfillment.entity.Trait;
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

    @NotNull
    @Override
    public SyncResponse onSync(SyncRequest request, Map<?, ?> headers) {
        Account account = accountService.findOrCreate(getTokenSubject());

        List<SyncResponse.Payload.Device> devices = account.getGateways()
                .stream()
                .flatMap(gateway -> gateway.getDevices().stream())
                .map(device -> new SyncResponse.Payload.Device.Builder()
                        .setId(device.getId().toString())
                        .setType(device.getDeviceType())
                        .setName(DeviceProto.DeviceNames.newBuilder()
                                .setName(device.getDeviceName())
                                .build())
                        .setTraits(device.getTraits()
                                .stream()
                                .map(Trait::getTrait)
                                .collect(Collectors.toList()))
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

            Map<String, Object> responseDevice = new HashMap<>();

            if (device != null) {
                responseDevice.put("status", "SUCCESS");
                responseDevice.put("online", true);

                for (State state : device.getStates()) {
                    if (state.getValue().equals("false") || state.getValue().equals("true")) {
                        responseDevice.put(state.getKey(), Boolean.valueOf(state.getValue()));
                    } else {
                        responseDevice.put(state.getKey(), state.getValue());
                    }
                }
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
    public ExecuteResponse onExecute(ExecuteRequest executeRequest, Map<?, ?> headers) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public void onDisconnect(DisconnectRequest disconnectRequest, Map<?, ?> headers) {
        throw new UnsupportedOperationException();
    }

    private String getTokenSubject() {
        Jwt token = (Jwt) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        return token.getSubject();
    }
}