package com.ticketscale.infrastructure.redis;

import com.ticketscale.application.ports.LockManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisLockManager implements LockManager {

    private final StringRedisTemplate redisTemplate;

    public RedisLockManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean acquireLock(String key, long expirationSeconds) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "LOCKED", Duration.ofSeconds(expirationSeconds));
        return success != null && success;
    }

    @Override
    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}
