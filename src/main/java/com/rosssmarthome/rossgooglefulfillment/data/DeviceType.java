package com.rosssmarthome.rossgooglefulfillment.data;

import java.util.List;

public enum DeviceType {
    RELAY_SINGLE,
    BCM_SINGLE,
    BCM_RGB,
    BCM_RGBW;

    public String getGoogleDeviceType() {
        switch (this) {
            case RELAY_SINGLE:
                return "action.devices.types.OUTLET";
            case BCM_SINGLE:
            case BCM_RGB:
            case BCM_RGBW:
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
            case BCM_RGB:
                return List.of("action.devices.traits.OnOff", "action.devices.traits.ColorSpectrum");
            case BCM_RGBW:
                return List.of("action.devices.traits.OnOff", "action.devices.traits.Brightness", "action.devices.traits.ColorSpectrum");
            default:
                throw new UnsupportedOperationException();
        }
    }

    public String getGoogleDeviceName() {
        switch (this) {
            case RELAY_SINGLE:
                return "Outlet";
            case BCM_SINGLE:
            case BCM_RGB:
            case BCM_RGBW:
                return "Light";
            default:
                throw new UnsupportedOperationException();
        }
    }
}
