package com.ticketscale.infrastructure.redis;

import com.ticketscale.application.port.out.CacheManager;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class RedisCacheManagerImpl implements CacheManager {

    private final org.springframework.cache.CacheManager springCacheManager;

    public RedisCacheManagerImpl(org.springframework.cache.CacheManager springCacheManager) {
        this.springCacheManager = springCacheManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, Object key, Supplier<T> supplier) {
        org.springframework.cache.Cache cache = springCacheManager.getCache(cacheName);
        if (cache == null) {
            return supplier.get();
        }

        org.springframework.cache.Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper != null) {
            return (T) wrapper.get();
        }

        T value = supplier.get();
        if (value != null) {
            cache.put(key, value);
        }
        return value;
    }

    @Override
    public void evict(String cacheName, Object key) {
        org.springframework.cache.Cache cache = springCacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    @Override
    public void clear(String cacheName) {
        org.springframework.cache.Cache cache = springCacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
