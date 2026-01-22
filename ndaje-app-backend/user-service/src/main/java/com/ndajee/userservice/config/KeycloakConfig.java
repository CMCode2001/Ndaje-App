package com.ndajee.userservice.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import jakarta.annotation.PreDestroy;
import org.keycloak.OAuth2Constants;
import lombok.Data;

@Configuration
@EnableConfigurationProperties(KeycloakConfig.KeycloakAdminProperties.class)
public class KeycloakConfig {
    private Keycloak keycloakClient;

    @Data
    @ConfigurationProperties(prefix = "keycloak.admin")
    public static class KeycloakAdminProperties {
        private String serverUrl;
        private String realm;
        private String clientId;
        private String clientSecret;
    }

    @Bean
    public Keycloak keycloak(KeycloakAdminProperties props) {

        this.keycloakClient = KeycloakBuilder.builder()
                .serverUrl(props.getServerUrl())
                .realm(props.getRealm())
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();

        return this.keycloakClient;
    }

    @PreDestroy
    public void close() {
        if (keycloakClient != null) {
            keycloakClient.close();
        }
    }
}
