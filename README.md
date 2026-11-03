# Product Omnichannel API

Catálogo autoral para a PDP Aurora: produto e variantes por SKU, preço por canal/localidade e estoque informativo, com PostgreSQL e Redis. Java 21, Spring Boot 3.5, JDBC, Flyway, Spring Security, Resilience4j e OpenAPI. Dados fictícios; nenhuma dependência de plataforma corporativa.

## Executar

O caminho recomendado é o stack integrado no [storefront-pdp](https://github.com/JeffeMartins/storefront-pdp), que sobe esta API, banco, cache e frontend sem precisar instalar Java no host.

Para desenvolvimento isolado: instale Java 21, use o wrapper e forneça PostgreSQL/Redis locais. Variáveis:

| Variável | Padrão |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:25432/catalog` |
| `DB_PASSWORD` | Obrigatória; fora do Git |
| `REDIS_HOST` | `localhost` |
| `REDIS_PORT` | `6379` |

O usuário do banco é `catalog`. Flyway cria as tabelas e o dataset de demonstração.

```bash
./mvnw spring-boot:run
./mvnw -B verify
```

PowerShell: `./mvnw.cmd`. Os testes usam PostgreSQL/Redis reais descartáveis com Testcontainers e requerem Docker. Em Linux, caso o clone não preserve o bit executável, execute `chmod +x mvnw`.

## Endpoints

No Compose integrado a API fica em http://localhost:8081; isolada usa 8080.

```bash
curl -sS 'http://localhost:8081/api/v1/products/FONE-BT-001'
curl -sS 'http://localhost:8081/api/v1/products/FONE-BT-001?channel=PICKUP&locationId=LOJA-PAULISTA'
curl -sS 'http://localhost:8081/api/v1/products/FONE-BT-AREIA/availability'
curl -sS 'http://localhost:8081/api/v1/locations'
```

Swagger: `/swagger-ui/index.html`; schema: `/v3/api-docs`; liveness: `/actuator/health/liveness`. Leituras são públicas; não há endpoints administrativos. Mutações são negadas e CSRF permanece habilitado.

`WEB` usa `CD-DEMO-01`. `PICKUP` aceita `LOJA-CENTRO` e `LOJA-PAULISTA`. Contexto inválido retorna 400, SKU inexistente 404, falha SQL 503. Areia é um produto esgotado e retorna 200; falta de estoque não significa recurso inexistente.

## Modelo e consistência

Produtos pertencem a uma família de variantes. Ofertas têm chave `(sku,channel,location_id)` e estoque `(sku,location_id)`. Foreign keys, preços `NUMERIC(12,2)`, checks de preço/quantidade e índices protegem os dados. Java usa `BigDecimal`; o JSON entrega preços como string decimal com moeda BRL. Um único vendedor fictício é assumido.

Redis usa JSON tipado: catálogo por SKU com TTL 300–329 s; oferta/estoque por contexto com TTL 10 s. Cache miss ou falha consulta PostgreSQL. Circuit breaker limita tentativas ao Redis com falhas; counters Micrometer registram hits, misses e erros. O contexto ainda é validado no banco: este projeto não promete eliminar leituras de banco. Não existe cache local adicional ou lock de refill.

O catálogo é somente leitura neste MVP. Mudar dados diretamente em SQL pode ficar invisível até o TTL expirar. Uma administração futura deve invalidar após commit, com testes de concorrência; não se deve remover migrations já aplicadas. Não há reserva nem checkout: confirmar compra exigiria revalidar preço e reservar estoque transacionalmente.

## Qualidade e limites

`mvn verify` aplica Spotless, executa testes JUnit/Testcontainers e gera JaCoCo. O CI também constrói a imagem multi-stage, executada sem root e com filesystem read-only no Compose.

As especificações visíveis de áudio são comuns à família fictícia e montadas no serviço; não há editor genérico de atributos, taxonomia completa ou busca facetada. O MVP usa SKU como chave natural; não reproduz o modelo corporativo do sistema usado como inspiração. Os bancos de catálogo e avaliações são independentes.

Não existe claim de desempenho sem benchmark, consistência forte de cache ou implantação comercial. Consultas são parametrizadas e entradas limitadas; exposição externa exige TLS, quotas, autenticação das futuras mutações e gestão de segredos. Licença MIT.
