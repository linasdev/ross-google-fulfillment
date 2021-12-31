package com.rosssmarthome.rossgooglefulfillment.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceState {
    @NotNull
    private Long peripheralAddress;

    @NotNull
    private Long peripheralIndex;

    @NotEmpty
    private PeripheralState peripheralState;
}
