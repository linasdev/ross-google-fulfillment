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
public class PeripheralState {
    @NotNull
    private DeviceType type;

    @NotEmpty
    @Builder.Default
    private Map<StateKey, String> payload = new HashMap<>();

    public Map<String, Object> getGoogleDeviceState() {
        switch (type) {
            case RELAY_SINGLE: {
                String on = payload.get(StateKey.ON);

                if (on == null) {
                    return null;
                }

                return Map.of(
                        "on", Boolean.valueOf(on)
                );
            }
            case BCM_SINGLE: {
                String on = payload.get(StateKey.ON);
                String brightness = payload.get(StateKey.BRIGHTNESS);

                if (brightness == null) {
                    return null;
                }

                return Map.of(
                        "on", Boolean.valueOf(on),
                        "brightness", Double.valueOf(brightness) / 255 * 100
                );
            }
            case BCM_RGB_B: {
                String on = payload.get(StateKey.ON);
                String red = payload.get(StateKey.RED);
                String green = payload.get(StateKey.GREEN);
                String blue = payload.get(StateKey.BLUE);
                String brightness = payload.get(StateKey.BRIGHTNESS);

                if (on == null || red == null || green == null || blue == null || brightness == null) {
                    return null;
                }

                return Map.of(
                        "on", Boolean.valueOf(on),
                        "color", Map.of(
                                "spectrumRGB", Long.valueOf(red) << 16 + Long.valueOf(green) << 8 + Long.valueOf(blue)
                        ),
                        "brightness", brightness
                );
            }
            case BCM_RGBW_B: {
                String on = payload.get(StateKey.ON);
                String red = payload.get(StateKey.RED);
                String green = payload.get(StateKey.GREEN);
                String blue = payload.get(StateKey.BLUE);
                String white = payload.get(StateKey.WHITE);
                String brightness = payload.get(StateKey.BRIGHTNESS);

                if (on == null || red == null || green == null || blue == null || white == null || brightness == null) {
                    return null;
                }

                return Map.of(
                        "on", Boolean.valueOf(on),
                        "color", Map.of(
                                "spectrumRGB", Long.valueOf(red + white) << 16 + Long.valueOf(green + white) << 8 + Long.valueOf(blue + white)
                        ),
                        "brightness", Double.valueOf(brightness) / 255 * 100
                );
            }
            default:
                throw new UnsupportedOperationException();
        }
    }
}
