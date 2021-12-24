package com.rosssmarthome.rossgooglefulfillment.entity;

import com.rosssmarthome.rossgooglefulfillment.data.StateKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "state")
@NoArgsConstructor
@AllArgsConstructor
public class State {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @Column(name = "state_key")
    @Enumerated(EnumType.STRING)
    private StateKey key;

    @Column(name = "state_value")
    private String value;
}
