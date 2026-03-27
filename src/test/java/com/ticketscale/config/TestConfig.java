package com.ticketscale.config;

import com.ticketscale.application.port.out.LockManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public LockManager lockManager() {
        return new LockManager() {
            @Override
            public boolean acquireLock(String key, long expirationSeconds) {
                return true;
            }

            @Override
            public void releaseLock(String key) {
                // Do nothing
            }
        };
    }
}
