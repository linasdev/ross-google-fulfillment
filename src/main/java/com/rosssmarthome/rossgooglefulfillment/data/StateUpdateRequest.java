package com.rosssmarthome.rossgooglefulfillment.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateUpdateRequest {
    @NotEmpty
    @Builder.Default
    private Map<String, String> states = new HashMap<>();
}
