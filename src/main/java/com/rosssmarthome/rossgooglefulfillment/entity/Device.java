package com.rosssmarthome.rossgooglefulfillment.entity;

import com.rosssmarthome.rossgooglefulfillment.data.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private DeviceType type;

    @Column(name = "peripheral_address")
    private Long peripheralAddress;

    @Column(name = "peripheral_index")
    private Long peripheralIndex;

    @Column(name = "synced_to_homegraph")
    private Boolean syncedToHomegraph;

    @Column(name = "last_seen")
    @UpdateTimestamp
    private LocalDateTime lastSeen;
}
