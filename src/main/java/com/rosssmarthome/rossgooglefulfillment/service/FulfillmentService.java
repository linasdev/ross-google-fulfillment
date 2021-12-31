package com.rosssmarthome.rossgooglefulfillment.service;

import com.google.actions.api.smarthome.*;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.homegraph.v1.HomeGraphService;
import com.google.api.services.homegraph.v1.model.AgentDeviceId;
import com.google.api.services.homegraph.v1.model.QueryRequestInput;
import com.google.api.services.homegraph.v1.model.QueryRequestPayload;
import com.google.home.graph.v1.DeviceProto;
import com.rosssmarthome.rossgooglefulfillment.entity.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FulfillmentService extends SmartHomeApp {
    private final AccountService accountService;
    private final HomeGraphService homeGraphService;
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

        List<QueryRequestInput> inputs = Stream.of(request.getInputs())
                .map(input -> (QueryRequest.Inputs) input)
                .map(input -> {
                    QueryRequestPayload payload = new QueryRequestPayload();
                    payload.setDevices(Arrays.stream(input.getPayload().getDevices())
                            .map(device -> {
                                AgentDeviceId id = new AgentDeviceId();
                                id.setId(device.getId());
                                return id;
                            })
                            .collect(Collectors.toList())
                    );

                    QueryRequestInput newInput = new QueryRequestInput();
                    newInput.setPayload(payload);
                    return newInput;
                })
                .collect(Collectors.toList());

        com.google.api.services.homegraph.v1.model.QueryRequest homeGraphRequest = new com.google.api.services.homegraph.v1.model.QueryRequest();
        homeGraphRequest.setRequestId(request.getRequestId());
        homeGraphRequest.setAgentUserId(account.getId().toString());
        homeGraphRequest.setInputs(inputs);

        com.google.api.services.homegraph.v1.model.QueryResponse homeGraphResponse;

        try {
            homeGraphResponse = homeGraphService.devices().query(homeGraphRequest).execute();
        } catch (IOException e) {
            log.warn("Failed to query HomeGraph device state", e);
            return null;
        }

        QueryResponse.Payload payload = new QueryResponse.Payload(homeGraphResponse.getPayload().getDevices());
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
