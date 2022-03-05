package com.rosssmarthome.rossgooglefulfillment.data;

import java.util.List;

public enum DeviceType {
    RELAY_SINGLE,
    BCM_SINGLE,
    BCM_RGB_B,
    BCM_RGBW_B;

    public String getGoogleDeviceType() {
        switch (this) {
            case RELAY_SINGLE:
                return "action.devices.types.OUTLET";
            case BCM_SINGLE:
            case BCM_RGB_B:
            case BCM_RGBW_B:
                return "action.devices.types.LIGHT";
            default:
                throw new UnsupportedOperationException();
        }
    }

    public List<String> getGoogleDeviceTraits() {
        switch (this) {
            case RELAY_SINGLE:
                return List.of("action.devices.traits.OnOff");
            case BCM_SINGLE:
                return List.of("action.devices.traits.OnOff", "action.devices.traits.Brightness");
            case BCM_RGB_B:
                return List.of("action.devices.traits.OnOff", "action.devices.traits.ColorSpectrum", "action.devices.traits.Brightness");
            case BCM_RGBW_B:
                return List.of("action.devices.traits.OnOff", "action.devices.traits.ColorSpectrum", "action.devices.traits.Brightness");
            default:
                throw new UnsupportedOperationException();
        }
    }

    public String getGoogleDeviceName() {
        switch (this) {
            case RELAY_SINGLE:
                return "Outlet";
            case BCM_SINGLE:
            case BCM_RGB_B:
            case BCM_RGBW_B:
                return "Light";
            default:
                throw new UnsupportedOperationException();
        }
    }
}
