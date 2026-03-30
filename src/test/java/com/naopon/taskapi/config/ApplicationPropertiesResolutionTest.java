package com.naopon.taskapi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;

class ApplicationPropertiesResolutionTest {

    @Test
    void resolvesDatasourceSettingsFromDiscreteEnvironmentVariables() throws IOException {
        PropertySourcesPropertyResolver resolver = propertyResolver(Map.of(
                "DB_HOST", "db",
                "DB_PORT", "15432",
                "DB_NAME", "portfolio"
        ));

        assertEquals("jdbc:postgresql://db:15432/portfolio", resolver.getProperty("spring.datasource.url"));
        assertEquals("app", resolver.getProperty("spring.datasource.username"));
        assertEquals("change-me", resolver.getProperty("spring.datasource.password"));
    }

    @Test
    void prefersExplicitDbUrlWhenProvided() throws IOException {
        PropertySourcesPropertyResolver resolver = propertyResolver(Map.of(
                "DB_URL", "jdbc:postgresql://override-host:25432/override-db",
                "DB_HOST", "db",
                "DB_PORT", "15432",
                "DB_NAME", "portfolio"
        ));

        assertEquals("jdbc:postgresql://override-host:25432/override-db", resolver.getProperty("spring.datasource.url"));
    }

    @Test
    void importsDotEnvForLocalBootRun() throws IOException {
        PropertySourcesPropertyResolver resolver = propertyResolver(Map.of());

        assertEquals("optional:file:.env[.properties]", resolver.getProperty("spring.config.import"));
    }

    @Test
    void resolvesServerPortFromPortEnvironmentVariable() throws IOException {
        PropertySourcesPropertyResolver resolver = propertyResolver(Map.of(
                "PORT", "18080"
        ));

        assertEquals("18080", resolver.getProperty("server.port"));
    }

    @Test
    void enablesForwardHeadersForProxyAwareDeployments() throws IOException {
        PropertySourcesPropertyResolver resolver = propertyResolver(Map.of());

        assertEquals("framework", resolver.getProperty("server.forward-headers-strategy"));
    }

    private PropertySourcesPropertyResolver propertyResolver(Map<String, String> environmentValues) throws IOException {
        MutablePropertySources propertySources = new MutablePropertySources();
        propertySources.addFirst(new MapPropertySource("environment", new HashMap<>(environmentValues)));
        propertySources.addLast(new PropertiesPropertySource("applicationProperties", loadMainApplicationProperties()));
        return new PropertySourcesPropertyResolver(propertySources);
    }

    private Properties loadMainApplicationProperties() throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("src/main/resources/application.properties"))) {
            properties.load(reader);
        }
        return properties;
    }
}
