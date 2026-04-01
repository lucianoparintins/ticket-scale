package com.ticketscale.application.port.out;

import java.util.function.Supplier;

/**
 * Interface que define as operações de cache para a camada de aplicação.
 * Segue a estratégia Cache-aside (Lazy Loading).
 */
public interface CacheManager {

    /**
     * Busca um item no cache. Se não existir, executa o fornecedor, popula o cache e retorna o resultado.
     *
     * @param cacheName Nome do cache (ex: "eventos")
     * @param key Chave do item no cache
     * @param supplier Fornecedor do dado caso ocorra cache miss
     * @param <T> Tipo do dado
     * @return O dado encontrado ou recuperado do fornecedor
     */
    <T> T get(String cacheName, Object key, Supplier<T> supplier);

    /**
     * Remove um item específico do cache.
     *
     * @param cacheName Nome do cache
     * @param key Chave do item
     */
    void evict(String cacheName, Object key);

    /**
     * Remove todos os itens de um cache específico.
     *
     * @param cacheName Nome do cache
     */
    void clear(String cacheName);
}
