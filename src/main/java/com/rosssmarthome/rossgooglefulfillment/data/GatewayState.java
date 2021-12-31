package com.rosssmarthome.rossgooglefulfillment.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayState {
    @NotEmpty
    @Builder.Default
    @Valid
    private List<@Valid DeviceState> deviceStates = new ArrayList<>();
}
