package com.rosssmarthome.rossgooglefulfillment.service;

import com.google.actions.api.smarthome.*;
import com.google.home.graph.v1.DeviceProto;
import com.rosssmarthome.rossgooglefulfillment.entity.Account;
import com.rosssmarthome.rossgooglefulfillment.entity.Trait;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FulfillmentService extends SmartHomeApp {
    private final AccountService accountService;

    @Override
    public SyncResponse onSync(SyncRequest syncRequest, Map<?, ?> headers) {
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
        SyncResponse response = new SyncResponse(syncRequest.getRequestId(), payload);

        return response;
    }

    @Override
    public QueryResponse onQuery(QueryRequest queryRequest, Map<?, ?> headers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExecuteResponse onExecute(ExecuteRequest executeRequest, Map<?, ?> headers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onDisconnect(DisconnectRequest disconnectRequest, Map<?, ?> headers) {
        throw new UnsupportedOperationException();
    }

    private String getTokenSubject() {
        Jwt token = (Jwt) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        return token.getSubject();
    }
}