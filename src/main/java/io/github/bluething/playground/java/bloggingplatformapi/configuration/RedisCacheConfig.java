package io.github.bluething.playground.java.bloggingplatformapi.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(RedisCacheProperties.class)
@EnableCaching
@Slf4j
public class RedisCacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, RedisCacheProperties redisCacheProperties) {
        // Use ObjectMapper with optimized settings for caching
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        // Create serializer with custom ObjectMapper
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration
        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Increased default TTL
                .prefixCacheNameWith(redisCacheProperties.getKeyPrefix() + ":") // Use application-specific prefix
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .computePrefixWith(cacheName -> redisCacheProperties.getKeyPrefix() + ":" + cacheName + ":"); // Custom prefix computation

        // Configure null value handling based on properties
        final RedisCacheConfiguration defaultConfig = redisCacheProperties.isEnableNullValues()
                ? baseConfig
                : baseConfig.disableCachingNullValues();

        // Cache-specific configurations from properties
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        redisCacheProperties.getCaches().forEach((cacheName, cacheConfig) -> {
            RedisCacheConfiguration config = defaultConfig;

            // Apply cache-specific TTL if configured
            if (cacheConfig.getTtl() != null) {
                config = config.entryTtl(cacheConfig.getTtl());
            }

            // Apply cache-specific prefix if configured
            if (cacheConfig.getPrefix() != null) {
                config = config.prefixCacheNameWith(cacheConfig.getPrefix() + ":");
            }

            // Apply cache-specific null value handling
            if (!cacheConfig.isAllowNullValues()) {
                config = config.disableCachingNullValues();
            }

            cacheConfigurations.put(cacheName, config);
        });

        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations);

        // Enable transaction support based on properties
        if (redisCacheProperties.isEnableTransactions()) {
            builder = builder.transactionAware();
        }

        return builder.build();
    }
}
