package com.rosssmarthome.rossgooglefulfillment.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCommand {
    @NotNull
    private Long peripheralAddress;

    @NotNull
    private Long peripheralIndex;

    @NotNull
    private CommandType type;

    @NotEmpty
    @Builder.Default
    private Map<CommandPayloadKey, Object> payload = new HashMap<>();

    public static DeviceCommand from(String command, Map<String, Object> params, Long peripheralAddress, Long peripheralIndex, DeviceType deviceType) {
        DeviceCommand deviceCommand = DeviceCommand.builder()
                .peripheralAddress(peripheralAddress)
                .peripheralIndex(peripheralIndex)
                .build();

        switch (command) {
            case "action.devices.commands.OnOff": {
                CommandType commandType;

                boolean on = (boolean) params.get("on");

                switch (deviceType) {
                    case RELAY_SINGLE:
                        commandType = CommandType.RELAY_SET_SINGLE;
                        break;
                    case BCM_SINGLE:
                    case BCM_RGB_B:
                    case BCM_RGBW_B:
                        commandType = CommandType.BCM_SET_BINARY;
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }

                deviceCommand.getPayload().put(
                        CommandPayloadKey.VALUE,
                       on
                );

                deviceCommand.setType(commandType);
                break;
            }
            case "action.devices.commands.BrightnessAbsolute": {
                Integer brightness = (int) params.get("brightness");

                deviceCommand.setType(CommandType.BCM_SET_SINGLE);
                deviceCommand.getPayload().put(
                        CommandPayloadKey.VALUE,
                        (long) (brightness.doubleValue() / 100 * 255)
                );
                break;
            }

            case "action.devices.commands.ColorAbsolute": {
                Map<String, Object> colorMap = (Map<String, Object>) params.get("color");
                Long hexColor = (long) ((int) colorMap.get("spectrumRGB"));

                switch (deviceType) {
                    case BCM_RGB_B: {
                        deviceCommand.setType(CommandType.BCM_SET_RGB);
                        deviceCommand.getPayload().put(
                                CommandPayloadKey.RED,
                                hexColor >> 16 & 0xff
                        );
                        deviceCommand.getPayload().put(
                                CommandPayloadKey.GREEN,
                                hexColor >> 8 & 0xff
                        );
                        deviceCommand.getPayload().put(
                                CommandPayloadKey.BLUE,
                                hexColor & 0xff
                        );
                        break;
                    }

                    case BCM_RGBW_B: {
                        Long red = hexColor >> 16 & 0xff;
                        Long green = hexColor >> 8 & 0xff;
                        Long blue = hexColor & 0xff;

                        Long minValue = Long.min(red, Long.min(green, blue));

                        deviceCommand.setType(CommandType.BCM_SET_RGBW);
                        deviceCommand.getPayload().put(CommandPayloadKey.RED, red - minValue);
                        deviceCommand.getPayload().put(CommandPayloadKey.GREEN, green - minValue);
                        deviceCommand.getPayload().put(CommandPayloadKey.BLUE, blue - minValue);
                        deviceCommand.getPayload().put(CommandPayloadKey.WHITE, minValue);
                        break;
                    }

                    default:
                        throw new UnsupportedOperationException();
                }

                break;
            }
            default:
                throw new UnsupportedOperationException();
        }

        return deviceCommand;
    }
}
