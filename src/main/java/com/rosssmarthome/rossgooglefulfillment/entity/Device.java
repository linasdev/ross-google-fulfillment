package com.rosssmarthome.rossgooglefulfillment.entity;

import com.rosssmarthome.rossgooglefulfillment.data.DeviceType;
import com.rosssmarthome.rossgooglefulfillment.data.StateKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "device")
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gateway_id")
    private Gateway gateway;

    @Column(name = "peripheral_address")
    private Long peripheralAddress;

    @Column(name = "peripheral_index")
    private Long peripheralIndex;

    @Column(name = "device_type")
    @Enumerated(EnumType.STRING)
    private DeviceType type;

    @Column(name = "device_name")
    private String name;

    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<State> states = new ArrayList<>();

    public State findStateByKey(StateKey stateKey) {
        return states.stream().filter(state -> state.getKey().equals(stateKey)).findFirst().orElse(null);
    }

    public Map<String, Object> getGoogleDeviceState() {
        switch (type) {
            case RELAY_SINGLE: {
                State on = findStateByKey(StateKey.ON);

                if (on == null) {
                    return null;
                }

                return Map.of(
                        "on", Boolean.valueOf(on.getValue())
                );
            }
            case BCM_SINGLE: {
                State on = findStateByKey(StateKey.ON);
                State brightness = findStateByKey(StateKey.BRIGHTNESS);

                if (on == null || brightness == null) {
                    return null;
                }

                return Map.of(
                        "on", Boolean.valueOf(on.getValue()),
                        "brightness", Long.valueOf(brightness.getValue())
                );
            }
            case BCM_RGB: {
                State on = findStateByKey(StateKey.ON);
                State red = findStateByKey(StateKey.RED);
                State green = findStateByKey(StateKey.GREEN);
                State blue = findStateByKey(StateKey.BLUE);

                if (on == null || red == null || green == null || blue == null) {
                    return null;
                }

                return Map.of(
                        "on", Boolean.valueOf(on.getValue()),
                        "color", Map.of(
                                "spectrumRgb", Long.valueOf(red.getValue()) << 16 + Long.valueOf(green.getValue()) << 8 + Long.valueOf(blue.getValue())
                        )
                );
            }
            default:
                throw new UnsupportedOperationException();
        }
    }

    public void clearStates() {
        this.states.clear();
    }

    public void addState(State state) {
        if (state != null) {
            state.setDevice(this);
            states.add(state);
        }
    }
}
