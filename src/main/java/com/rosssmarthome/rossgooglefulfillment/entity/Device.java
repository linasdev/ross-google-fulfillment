package com.rosssmarthome.rossgooglefulfillment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
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
    private String deviceType;

    @Column(name = "device_name")
    private String deviceName;

    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Trait> traits = new ArrayList<>();

    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<State> states = new ArrayList<>();

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
