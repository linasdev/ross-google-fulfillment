package com.rosssmarthome.rossgooglefulfillment.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.rosssmarthome.rossgooglefulfillment.data.GatewayState;
import com.rosssmarthome.rossgooglefulfillment.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class GcpConfig {
    @Bean
    public CloudIot cloudIot() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("gcp_credentials.json");
        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream).createScoped(CloudIotScopes.CLOUDIOT);
        HttpRequestInitializer init = new HttpCredentialsAdapter(googleCredentials);

        CloudIot cloudIot = new CloudIot.Builder(httpTransport, jsonFactory, init).build();

        return cloudIot;
    }

    @Bean
    @Transactional
    public Consumer<Message<GatewayState>> handleNewGatewayState(GatewayService gatewayService) {
        return message -> {
            String deviceIdHeader = message.getHeaders().get("deviceId", String.class);

            if (deviceIdHeader == null) {
                log.warn("No device id header found with new gateway state request");
                return;
            }

            UUID gatewayId;

            try {
                gatewayId = UUID.fromString(deviceIdHeader);
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid device id header found with new gateway state request", ex);
                return;
            }

            log.info("Processing new gateway state request for gateway with id: {}", gatewayId);

            gatewayService.handleNewState(gatewayId, message.getPayload());
        };
    }

    @Bean
    public Consumer<String> handleDeadLetter() {
        return deadLetter -> {
            log.warn("Pub/Sub dead letter received: {}", deadLetter);
        };
    }
}
