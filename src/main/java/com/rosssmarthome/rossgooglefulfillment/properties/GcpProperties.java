package com.rosssmarthome.rossgooglefulfillment.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "gcp")
public class GcpProperties {
    private String projectId;
    private String region;
    private String registryId;
}
