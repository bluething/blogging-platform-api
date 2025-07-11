package io.github.bluething.playground.java.bloggingplatformapi.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app.cache.redis")
public class RedisCacheProperties {
    private Duration defaultTtl = Duration.ofMinutes(1);
    private String keyPrefix = "post";
    private boolean enableNullValues = false;
    private boolean enableTransactions = true;

    // Cache-specific settings
    private Map<String, CacheConfig> caches = new HashMap<>();

    @Data
    public static class CacheConfig {
        private Duration ttl;
        private String prefix;
        private boolean allowNullValues = false;
    }
}
