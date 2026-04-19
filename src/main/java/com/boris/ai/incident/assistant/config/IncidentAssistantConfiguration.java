package com.boris.ai.incident.assistant.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IncidentAssistantProperties.class)
public class IncidentAssistantConfiguration {
}
