# Nginx Load Balancer - TicketScale

Este diretório contém as configurações do Nginx para load balancing e reverse proxy da API TicketScale.

## Arquivos

- `nginx.conf` - Configuração principal (desenvolvimento)
- `nginx.prod.conf` - Configuração para produção (Docker Swarm)
- `upstream.conf` - Configuração de upstreams (desenvolvimento)
- `conf.d/default.conf` - Configuração do servidor virtual

## Arquitetura

### Desenvolvimento (docker-compose.yml)
- 1 instância da API
- Nginx com configuração estática
- Acesso direto via container name

### Produção (docker-compose.prod.yml)
- Múltiplas instâncias da API (2-10 réplicas)
- Nginx usando DNS round-robin do Docker Swarm
- Auto-scaling baseado em CPU/Memory

## Uso

### Desenvolvimento

```bash
# Iniciar todos os serviços (incluindo nginx)
docker compose up -d

# Acessar API através do nginx
curl http://localhost/api/eventos

# Health check do nginx
curl http://localhost/health
```

### Produção (Docker Swarm)

```bash
# 1. Inicializar Docker Swarm (se necessário)
docker swarm init

# 2. Fazer deploy do stack
docker stack deploy -c docker-compose.prod.yml ticketscale

# 3. Verificar serviços
docker service ls

# 4. Ver logs do nginx
docker service logs ticketscale_nginx

# 5. Escalar manualmente a API
docker service scale ticketscale_api=5

# 6. Auto-scaling automático (script)
./scripts/auto-scale.sh 2 10 70

# 7. Remover stack
docker stack rm ticketscale
```

## Configurações

### Rate Limiting

| Endpoint | Limite | Burst |
|----------|--------|-------|
| `/api/auth/**` | 10 req/s | 20 |
| `/api/**` | 100 req/s | 50 |

### Timeouts

- Connect: 60s
- Send: 60s
- Read: 60s (120s para /actuator)

### Health Checks

- Nginx: `http://localhost/health` → 200 OK
- API: `http://localhost:8080/actuator/health`

## Auto-scaling

O Docker Swarm não possui auto-scaling nativo. Use uma das abordagens:

### 1. Script Automático

```bash
./scripts/auto-scale.sh 2 10 70
```

Parâmetros:
- `2`: Réplicas mínimas
- `10`: Réplicas máximas
- `70`: Threshold de CPU (%)

### 2. Manual

```bash
# Escalar para 5 réplicas
docker service scale ticketscale_api=5

# Ver métricas
docker stats
```

### 3. Prometheus + Alertmanager

Configure alertas no Prometheus para trigger de scaling baseado em métricas.

## Logs

Os logs do nginx são armazenados em:

- Desenvolvimento: volume `nginx_logs`
- Produção: volume `nginx_logs` (acessível via `docker service logs`)

### Acessar Logs

```bash
# Desenvolvimento
docker logs ticketscale-nginx

# Produção
docker service logs ticketscale_nginx

# Follow
docker service logs -f ticketscale_nginx
```

## Segurança

Headers adicionados:
- `X-Frame-Options: SAMEORIGIN`
- `X-Content-Type-Options: nosniff`
- `X-XSS-Protection: 1; mode=block`
- `Referrer-Policy: strict-origin-when-cross-origin`

## Troubleshooting

### Nginx não inicia

Verifique se as configurações estão válidas:

```bash
docker compose run --rm nginx nginx -t
```

### Load balancing não funciona

1. Verifique health checks da API
2. Confirme que API está saudável: `curl http://localhost:8080/actuator/health`
3. Verifique logs do nginx

### Auto-scaling não escala

1. Verifique se Docker Swarm está ativo: `docker node ls`
2. Confirme nome do serviço: `docker service ls`
3. Verifique recursos disponíveis no cluster

## Monitoramento

### Métricas disponíveis

- Requests por segundo
- Response time (p50, p90, p99)
- Error rate (4xx, 5xx)
- Active connections

### Prometheus

O nginx pode exportar métricas via `nginx-prometheus-exporter`. Configure se necessário.

## Referências

- [Nginx Load Balancing](https://docs.nginx.com/nginx/admin-guide/load-balancer/http-load-balancer/)
- [Docker Swarm Networking](https://docs.docker.com/network/network-tutorial-overlay/)
- [Docker Swarm Service Discovery](https://docs.docker.com/engine/swarm/networking/#use-dns-round-robin-for-load-balancing)
