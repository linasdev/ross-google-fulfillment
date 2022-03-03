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
                String brightness = payload.get(StateKey.BRIGHTNESS);

                if (brightness == null) {
                    return null;
                }

                return Map.of(
                        "on", Long.valueOf(brightness) != 0,
                        "brightness", Double.valueOf(brightness) / 255 * 100
                );
            }
            case BCM_RGB: {
                String on = payload.get(StateKey.ON);
                String red = payload.get(StateKey.RED);
                String green = payload.get(StateKey.GREEN);
                String blue = payload.get(StateKey.BLUE);

                if (on == null || red == null || green == null || blue == null) {
                    return null;
                }

                return Map.of(
                        "on", Boolean.valueOf(on),
                        "color", Map.of(
                                "spectrumRGB", Long.valueOf(red) << 16 + Long.valueOf(green) << 8 + Long.valueOf(blue)
                        )
                );
            }
            case BCM_RGBW: {
                String on = payload.get(StateKey.ON);
                String red = payload.get(StateKey.RED);
                String green = payload.get(StateKey.GREEN);
                String blue = payload.get(StateKey.BLUE);
                String white = payload.get(StateKey.WHITE);

                if (on == null || red == null || green == null || blue == null) {
                    return null;
                }

                return Map.of(
                        "on", Boolean.valueOf(on),
                        "color", Map.of(
                                "spectrumRGB", Long.valueOf(red) << 16 + Long.valueOf(green) << 8 + Long.valueOf(blue)
                        ),
                        "brightness", Double.valueOf(white) / 255 * 100
                );
            }
            default:
                throw new UnsupportedOperationException();
        }
    }
}
