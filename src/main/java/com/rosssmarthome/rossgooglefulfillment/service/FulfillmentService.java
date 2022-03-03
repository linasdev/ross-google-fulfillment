package com.rosssmarthome.rossgooglefulfillment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.actions.api.smarthome.*;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.model.SendCommandToDeviceRequest;
import com.google.api.services.homegraph.v1.HomeGraphService;
import com.google.api.services.homegraph.v1.model.AgentDeviceId;
import com.google.api.services.homegraph.v1.model.QueryRequestInput;
import com.google.api.services.homegraph.v1.model.QueryRequestPayload;
import com.google.home.graph.v1.DeviceProto;
import com.rosssmarthome.rossgooglefulfillment.data.CommandType;
import com.rosssmarthome.rossgooglefulfillment.data.DeviceCommand;
import com.rosssmarthome.rossgooglefulfillment.data.GatewayCommand;
import com.rosssmarthome.rossgooglefulfillment.data.StateKey;
import com.rosssmarthome.rossgooglefulfillment.entity.Account;
import com.rosssmarthome.rossgooglefulfillment.entity.Device;
import com.rosssmarthome.rossgooglefulfillment.properties.GcpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FulfillmentService extends SmartHomeApp {
    private final AccountService accountService;
    private final DeviceService deviceService;
    private final HomeGraphService homeGraphService;
    private final CloudIot cloudIot;
    private final GcpProperties gcpProperties;
    private final ObjectMapper objectMapper;

    @NotNull
    @Override
    public SyncResponse onSync(SyncRequest request, Map<?, ?> headers) {
        log.info("Fulfilling SYNC request with id {}", request.getRequestId());

        Account account = accountService.findOrCreate(getTokenSubject());

        List<SyncResponse.Payload.Device> devices = account.getGateways()
                .stream()
                .flatMap(gateway -> gateway.getDevices().stream())
                .map(device -> new SyncResponse.Payload.Device.Builder()
                        .setId(device.getId().toString())
                        .setType(device.getType().getGoogleDeviceType())
                        .setTraits(device.getType().getGoogleDeviceTraits())
                        .setName(DeviceProto.DeviceNames.newBuilder()
                                .setName(device.getName())
                                .build())
                        .setWillReportState(true)
                        .build())
                .collect(Collectors.toList());

        SyncResponse.Payload payload = new SyncResponse.Payload(account.getId().toString(), devices.toArray(new SyncResponse.Payload.Device[0]));
        SyncResponse response = new SyncResponse(request.getRequestId(), payload);

        return response;
    }

    @NotNull
    @Override
    public QueryResponse onQuery(QueryRequest request, Map<?, ?> headers) {
        log.info("Fulfilling QUERY request with id {}", request.getRequestId());

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
        log.info("Fulfilling EXECUTE request with id {}", request.getRequestId());

        List<ExecuteRequest.Inputs.Payload.Commands> commands = Stream.of(request.getInputs())
                .map(input -> (ExecuteRequest.Inputs) input)
                .map(input -> input.getPayload())
                .flatMap(payload -> Arrays.stream(payload.getCommands()))
                .collect(Collectors.toList());

        List<ExecuteResponse.Payload.Commands> responseCommands = new ArrayList<>();

        for (ExecuteRequest.Inputs.Payload.Commands command : commands) {
            Map<UUID, GatewayCommand> gatewayCommands = new HashMap<>();

            for (ExecuteRequest.Inputs.Payload.Commands.Execution execution : command.getExecution()) {
                for (ExecuteRequest.Inputs.Payload.Commands.Devices executeDevice : command.getDevices()) {
                    Device device = deviceService.getById(UUID.fromString(executeDevice.getId()));
                    UUID gatewayId = device.getGateway().getId();

                    DeviceCommand deviceCommand = DeviceCommand.builder()
                            .peripheralAddress(device.getPeripheralAddress())
                            .peripheralIndex(device.getPeripheralIndex())
                            .build();

                    switch (execution.getCommand()) {
                        case "action.devices.commands.OnOff": {
                            CommandType commandType;

                            boolean on = (boolean) ((Map<String, Object>) execution.getParams()).get("on");

                            if (on) {
                                switch (device.getType()) {
                                    case RELAY_SINGLE:
                                        commandType = CommandType.RELAY_TURN_ON_SINGLE;
                                        break;
                                    case BCM_SINGLE:
                                    case BCM_RGB:
                                    case BCM_RGBW:
                                        commandType = CommandType.BCM_TURN_ON;
                                        break;
                                    default:
                                        throw new UnsupportedOperationException();
                                }
                            } else {
                                switch (device.getType()) {
                                    case RELAY_SINGLE:
                                        commandType = CommandType.RELAY_TURN_OFF_SINGLE;
                                        break;
                                    case BCM_SINGLE:
                                    case BCM_RGB:
                                    case BCM_RGBW:
                                        commandType = CommandType.BCM_TURN_OFF;
                                        break;
                                    default:
                                        throw new UnsupportedOperationException();
                                }
                            }

                            deviceCommand.setType(commandType);
                            break;
                        }
                        case "action.devices.commands.BrightnessAbsolute": {
                            CommandType commandType;

                            switch (device.getType()) {
                                case BCM_SINGLE:
                                    commandType = CommandType.BCM_SET_SINGLE;
                                    break;
                                case BCM_RGBW:
                                    commandType = CommandType.BCM_SET_WHITE;
                                    break;
                                default:
                                    throw new UnsupportedOperationException();
                            }

                            Integer brightness = (int) ((Map<String, Object>) execution.getParams()).get("brightness");

                            deviceCommand.setType(commandType);
                            deviceCommand.getPayload().put(
                                    StateKey.BRIGHTNESS,
                                    (long) (brightness.doubleValue() / 100 * 255)
                            );
                            break;
                        }

                        case "action.devices.commands.ColorAbsolute": {
                            Map<String, Object> colorMap = (Map<String, Object>) ((Map<String, Object>) execution.getParams()).get("color");
                            Integer hexColor = (int) colorMap.get("spectrumRGB");

                            deviceCommand.setType(CommandType.BCM_SET_RGB);
                            deviceCommand.getPayload().put(
                                    StateKey.RED,
                                    hexColor >> 16 & 0xff
                            );
                            deviceCommand.getPayload().put(
                                    StateKey.GREEN,
                                    hexColor >> 8 & 0xff
                            );
                            deviceCommand.getPayload().put(
                                    StateKey.BLUE,
                                    hexColor & 0xff
                            );

                            break;
                        }
                        default:
                            throw new UnsupportedOperationException();
                    }

                    if (gatewayCommands.containsKey(gatewayId)) {
                        GatewayCommand gatewayCommand = gatewayCommands.get(gatewayId);
                        gatewayCommand.getDeviceCommands().add(deviceCommand);
                    } else {
                        GatewayCommand gatewayCommand = GatewayCommand.builder().build();
                        gatewayCommand.getDeviceCommands().add(deviceCommand);
                        gatewayCommands.put(gatewayId, gatewayCommand);
                    }
                }

                responseCommands.add(new ExecuteResponse.Payload.Commands(
                        Arrays.stream(command.getDevices())
                                .map(ExecuteRequest.Inputs.Payload.Commands.Devices::getId)
                                .collect(Collectors.toList())
                                .toArray(new String[0]),
                        "SUCCESS",
                        null,
                        null,
                        null
                ));
            }

            gatewayCommands.forEach((gatewayId, gatewayCommand) -> {
                log.info("Sending command to gateway with id: {}", gatewayId);

                String name = String.format(
                        "projects/%s/locations/%s/registries/%s/devices/%s",
                        gcpProperties.getProjectId(),
                        gcpProperties.getRegion(),
                        gcpProperties.getRegistryId(),
                        gatewayId.toString()
                );

                SendCommandToDeviceRequest cloudIotRequest = new SendCommandToDeviceRequest();

                try {
                    String jsonData = objectMapper.writeValueAsString(gatewayCommand);
                    String encodedData = Base64.getEncoder().encodeToString(jsonData.getBytes(StandardCharsets.UTF_8));
                    cloudIotRequest.setBinaryData(encodedData);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to serialize gateway command", e);
                }

                try {
                    cloudIot.projects().locations().registries().devices().sendCommandToDevice(name, cloudIotRequest).execute();
                } catch (IOException e) {
                    log.warn("Failed to send command to Cloud IoT", e);
                }
            });
        }

        ExecuteResponse.Payload payload = new ExecuteResponse.Payload(responseCommands.toArray(new ExecuteResponse.Payload.Commands[0]));
        ExecuteResponse response = new ExecuteResponse(request.getRequestId(), payload);

        return response;
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
