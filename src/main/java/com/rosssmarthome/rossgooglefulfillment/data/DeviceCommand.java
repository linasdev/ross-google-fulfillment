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
    private Map<StateKey, Object> payload = new HashMap<>();
}
