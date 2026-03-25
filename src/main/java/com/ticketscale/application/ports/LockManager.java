package com.ticketscale.application.ports;

public interface LockManager {
    
    /**
     * Tenta adquirir um lock distribuído para a chave especificada.
     * @param key Chave de lock (ex: lote_id)
     * @param expirationSeconds Tempo em segundos antes de expirar o lock
     * @return true se o lock foi adquirido, false caso contrário
     */
    boolean acquireLock(String key, long expirationSeconds);

    /**
     * Libera o lock previamente adquirido.
     * @param key Chave de lock
     */
    void releaseLock(String key);
}
