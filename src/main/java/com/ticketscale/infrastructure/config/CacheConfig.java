package com.ticketscale.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    public static final String CACHE_EVENTOS = "eventos";
    public static final String CACHE_LOTES = "lotes";
    public static final String CACHE_DASHBOARD = "dashboard";

    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        // Em ambiente real, um erro de deserializacao no Redis nao deve derrubar o endpoint.
        // Tratamos como cache miss (e tentamos limpar a chave) para auto-recuperar.
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                if (isSerializationProblem(exception)) {
                    log.warn("Erro ao ler cache '{}' chave '{}'. Ignorando (cache miss) e removendo entrada.", cache.getName(), key, exception);
                    try {
                        cache.evict(key);
                    } catch (RuntimeException evictEx) {
                        log.warn("Falha ao remover entrada corrompida do cache '{}' chave '{}'.", cache.getName(), key, evictEx);
                    }
                    return;
                }
                throw exception;
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                if (isSerializationProblem(exception)) {
                    log.warn("Erro ao gravar cache '{}' chave '{}'. Ignorando.", cache.getName(), key, exception);
                    return;
                }
                throw exception;
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                if (isSerializationProblem(exception)) {
                    log.warn("Erro ao evict cache '{}' chave '{}'. Ignorando.", cache.getName(), key, exception);
                    return;
                }
                throw exception;
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                if (isSerializationProblem(exception)) {
                    log.warn("Erro ao limpar cache '{}'. Ignorando.", cache.getName(), exception);
                    return;
                }
                throw exception;
            }
        };
    }

    private static boolean isSerializationProblem(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof SerializationException) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configuração padrão: 5 minutos de TTL
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .prefixCacheNameWith("ticketscale:")
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));

        // Configurações específicas por cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Lotes: 2 minutos
        cacheConfigurations.put(CACHE_LOTES, defaultConfig.entryTtl(Duration.ofMinutes(2)));
        
        // Dashboard: 10 minutos
        cacheConfigurations.put(CACHE_DASHBOARD, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Eventos já usa o padrão de 5 minutos

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
