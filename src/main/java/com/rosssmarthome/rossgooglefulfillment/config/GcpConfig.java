package com.rosssmarthome.rossgooglefulfillment.config;

import com.rosssmarthome.rossgooglefulfillment.data.StateUpdateRequest;
import com.rosssmarthome.rossgooglefulfillment.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class GcpConfig {
    @Bean
    @Transactional
    public Consumer<Message<StateUpdateRequest>> handleStateUpdate(DeviceService deviceService) {
        return message -> {
            String deviceIdHeader = message.getHeaders().get("deviceId", String.class);

            if (deviceIdHeader == null) {
                log.warn("No device id header found with state update request");
                return;
            }

            UUID deviceId;

            try {
                deviceId = UUID.fromString(deviceIdHeader);
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid device id header found with state update request", ex);
                return;
            }

            log.info("Processing state update request for device with id: {}", deviceId);

            deviceService.handleStateUpdate(deviceId, message.getPayload());
        };
    }

    @Bean
    public Consumer<String> handleDeadLetter() {
        return deadLetter -> {
            log.warn("Pub/Sub dead letter received: {}", deadLetter);
        };
    }
}
