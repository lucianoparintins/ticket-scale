package com.ticketscale.domain.event;

public record CacheInvalidadoEvent(String cacheName, String key) {
}
