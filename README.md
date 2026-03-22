# Estapar Parking Management System

Sistema backend para gerenciamento de estacionamento com controle de vagas, entrada/saída de veículos, preço dinâmico e cálculo de receita.

## Stack

- **Kotlin 2.1.20** + **Java 21**
- **Micronaut 4.10.10** (HTTP server, Data JPA, HTTP client)
- **Micronaut Gradle Plugin 4.6.2**
- **MySQL 8** (persistência)
- **Flyway** (migrations)
- **Gradle Kotlin DSL** (multi-module)
- **Kotest** (BehaviorSpec) para testes
- **Docker Compose** para orquestração

## Arquitetura

### Hexagonal (Ports & Adapters) — enforced via Gradle

A separação hexagonal é **real e verificada em tempo de compilação**. O módulo `domain` não possui nenhuma dependência de framework.

```
parking-management/
├── domain/              → kotlin-stdlib APENAS. Zero Micronaut, zero JPA.
├── application/         → depende de :domain (use cases)
├── adapter-inbound/     → depende de :domain + :application (controllers REST)
├── adapter-outbound/    → depende de :domain + :application (JPA, HTTP client, Flyway)
├── bootstrap/           → depende de TODOS (composition root)
```

**Nenhum adapter depende do outro.** Toda dependência aponta para dentro (domain).

### Decisões Arquiteturais

| Decisão | Justificativa |
|---------|---------------|
| **Gradle multi-module** para hexagonal | Garante em compile-time que domain não importa framework |
| **`io.micronaut.library`** nos adapters | Apenas bootstrap é a application; adapters são libraries |
| **`@Factory` no bootstrap** | Domain objects não têm anotações DI; a factory os registra como beans |
| **Pipeline handlers recebem ports via construtor** | Preserva boundary hexagonal — sem `@Inject` no domain |
| **`ParkingStatus.ACTIVE_STATUSES` no domain** | Define quais status são "ativos" como regra de negócio, não decisão do adapter |
| **`SectorRepository.findAll()` + `Sector.isFull()`** | Regra de capacidade reside no domain; adapter apenas busca dados |
| **`WebhookEventType` enum** para dispatch de webhook | Elimina strings mágicas; `when` exaustivo sem `else` garante tratamento de todos os tipos em compile-time |
| **Inserção idempotente no startup** | `saveAll` verifica existência individual antes de inserir; permite reiniciar sem duplicatas |
| **Generated column + unique index para sessão ativa única** | MySQL não suporta partial unique index; coluna gerada `active_license_plate` é `license_plate` quando `exit_time IS NULL` e `NULL` caso contrário — garante no máximo 1 sessão ativa por placa no banco, prevenindo race conditions que o application layer sozinho não cobre |
| **Optimistic locking (`@Version`) em todas as entidades** | Coluna `version` em `spot`, `parking_session` e `sector`; Hibernate detecta conflitos automaticamente. Adapters usam padrão load-then-update para preservar a version gerenciada pelo Hibernate |
| **Transações explícitas nos use cases** | `@Transactional` em cada use case garante atomicidade do pipeline inteiro (reads + writes); `RevenueQuery` usa `SUPPORTS` por ser read-only |
| **Validação de input na borda** | `@Valid` + Bean Validation no controller; regex na `LicensePlate` aceita formato brasileiro (`ABC-1234`, `ABC1D23`) e formatos do simulador (`XX12345`, `XXX12345`); `@NotBlank` no `event_type` |
| **Timestamps flexíveis** | `EventMapper.toInstant()` aceita ISO-8601 com timezone (`2025-01-01T10:00:00Z`) e sem timezone (`2025-01-01T10:00:00`), assumindo UTC como fallback |
| **Clock injetável** | `java.time.Clock` registrado como bean via `UseCaseFactory`; use cases e controllers usam `Instant.now(clock)` em vez de `Instant.now()`, permitindo controle de tempo em testes |
| **StartupLoader com retry e fail-fast** | 3 tentativas com backoff (1s, 3s, 5s); se todas falham, lança exceção e impede o startup |

## Design Patterns

### Chain of Responsibility — Pipelines de ENTRY, PARKED e EXIT

Cada pipeline é uma lista ordenada de handlers. Interface no domain:

```kotlin
interface PipelineHandler<T> {
    fun handle(context: T, next: (T) -> T): T
}
```

**Entry Pipeline:** CheckDuplicateEntry → CheckSectorCapacity → CalculateDynamicPrice → CreateSession

**Parked Pipeline:** FindActiveSessionForParked → FindSpotByCoordinates → OccupySpot → ParkSession

**Exit Pipeline:** FindActiveSession → CalculateCharge → ReleaseSpot → CloseSession

Os três fluxos seguem o mesmo padrão arquitetural: reads primeiro, writes depois. Toda regra de negócio (capacidade de setor, status ativos, ocupação de vaga) reside no domain — adapters apenas persistem e buscam dados.

### Object Calisthenics

- **Primitivos encapsulados** em `@JvmInline value class` (LicensePlate, Money, SectorName, OccupancyRate, Coordinates)
- **Sem `var` público** nos domain models — mutação via métodos de negócio (`park()`, `exit()`, `occupy()`)
- **Contextos de pipeline imutáveis** — todos os campos são `val`; handlers propagam estado via `copy()`
- **Backing properties** para encapsular estado interno
- **Early return** e `when` exhaustivo ao invés de `else`
- **Ports usam Value Objects** nos contratos (ex: `Coordinates` em vez de `lat/lng` primitivos)

## Regras de Negócio

### Preço Dinâmico (calculado na ENTRY, por setor)

| Ocupação do Setor | Ajuste |
|---|---|
| < 25% | -10% (desconto) |
| 25% até 50% | 0% (preço base) |
| 50% até 75% | +10% (acréscimo) |
| 75% até 100% | +25% (acréscimo) |

### Cobrança (calculada na EXIT)

- Primeiros **30 minutos**: GRÁTIS
- Após 30 minutos: `ceil(duração em horas) × price_at_entry`

### Startup — Carga Idempotente da Garagem com Retry

No boot, o `StartupLoader` busca a configuração do simulador (setores e spots) e persiste no banco. A inserção é **idempotente por elemento**:

- **Setores**: verifica `findByName()` antes de inserir. Se já existe, pula com log.
- **Spots**: verifica `findByCoordinates(lat, lng)` antes de inserir. Se já existe, pula com log.
- Falha em um elemento individual é logada como warn e não impede os demais.
- **Retry com backoff**: 3 tentativas (1s, 3s, 5s). Se todas falham, a aplicação **não inicia** (fail-fast).

Isso garante que reiniciar a aplicação (ou múltiplas instâncias) nunca causa erro de duplicate key.

### Fluxo de Eventos

1. **ENTRY** → verifica duplicata → busca setor com capacidade (via domain `Sector.isFull()`) → calcula preço dinâmico → cria session
2. **PARKED** → encontra session ativa pela placa → localiza spot por coordenadas → ocupa vaga (`Spot.occupy()`) → atualiza session para PARKED
3. **EXIT** → encontra session ativa → calcula cobrança → libera vaga → fecha session

## API

### Webhook

```
POST http://localhost:3003/webhook
Content-Type: application/json

{ "event_type": "ENTRY", "license_plate": "ABC-1234", "entry_time": "2025-01-01T10:00:00Z" }
{ "event_type": "PARKED", "license_plate": "ABC-1234", "lat": -23.5505, "lng": -46.6333 }
{ "event_type": "EXIT", "license_plate": "ABC-1234", "exit_time": "2025-01-01T12:00:00Z" }
```

**Respostas:**

| Status | Quando |
|--------|--------|
| `200 OK` | Evento processado com sucesso, ou `event_type` desconhecido (ignorado com log warn) |
| `400 Bad Request` | Campos obrigatórios ausentes ou inválidos (ex: `lat`/`lng` faltando no PARKED, `license_plate` null, `event_type` em branco, placa fora do formato `ABC-1234` / `ABC1D23`) |
| `404 Not Found` | Veículo não encontrado — sem session ativa para a placa, ou coordenadas sem spot cadastrado |
| `422 Unprocessable Entity` | Regra de negócio violada (ver tabela abaixo) |
| `500 Internal Server Error` | Erro inesperado |

**Erros de negócio (422):**

| Código | Descrição |
|--------|-----------|
| `DUPLICATE_ENTRY` | Veículo já possui session ativa (ENTRY duplicado) |
| `SECTOR_FULL` | Todos os setores estão lotados |
| `SPOT_OCCUPIED` | A vaga nas coordenadas já está ocupada |
| `VEHICLE_ALREADY_PARKED` | Veículo já está com status PARKED (PARKED duplicado) |
| `INVALID_SESSION_STATE` | Transição de estado inválida (ex: EXIT em veículo com status ENTERED) |

**Formato de erro:**

```json
{ "error": "SECTOR_FULL", "message": "All sectors are full" }
```

### Revenue

```
GET http://localhost:3003/revenue?date=2025-01-01&sector=A
```

**Respostas:**

| Status | Quando |
|--------|--------|
| `200 OK` | Sucesso |
| `400 Bad Request` | Parâmetros `date` ou `sector` ausentes ou formato inválido |
| `500 Internal Server Error` | Erro inesperado |

**Exemplo de resposta (200):**

```json
{ "amount": 150.00, "currency": "BRL", "timestamp": "2025-01-01T12:00:00.000Z" }
```

## Como Rodar

### Pré-requisitos

- Docker e Docker Compose
- (Opcional) Java 21 + Gradle 8.12 para desenvolvimento local

### Com Docker Compose (recomendado)

```bash
docker-compose up --build
```

Isso inicia:
- **MySQL 8** na porta 3306
- **App** na porta 3003 (com Flyway migrations automáticas)
- **Simulador** (`cfontes0estapar/garage-sim:1.0.0`) — usa `network_mode: "service:app"` para compartilhar a rede com o app.

### Desenvolvimento Local

1. Inicie o MySQL:
```bash
docker-compose up mysql
```

2. Compile e rode os testes (requer Docker para testes de integração via Testcontainers):
```bash
./gradlew build
```

3. Rode apenas os testes de domain (sem banco, sem Docker):
```bash
./gradlew :domain:test
```

4. Rode apenas os testes de integração (requer Docker):
```bash
./gradlew :bootstrap:test
```

5. Inicie a aplicação:
```bash
./gradlew :bootstrap:run
```

## Estrutura de Testes

### Testes de Domain (Kotest BehaviorSpec, sem framework)

- **PricingServiceBehaviorTest** — 4 faixas de ocupação + 5 cenários de cobrança
- **ParkingSessionTest** — validações de transição de estado (ENTERED→PARKED→EXITED)
- **SpotTest** — ocupação e liberação de vagas, exceção para vaga já ocupada
- **EntryPipelineTest** — setor cheio, setor com vagas, ocupação 60%
- **ParkedPipelineTest** — sem sessão ativa, coordenadas sem vaga, vaga já ocupada, sucesso
- **ExitPipelineTest** — veículo não encontrado, saída normal com cobrança

### Testes de Application (Kotest BehaviorSpec, sem framework)

- **VehicleParkedUseCaseTest** — valida que o `parkedTime` é controlado pelo `Clock` injetado (não usa `Instant.now()` real)

### Testes de Integração (@MicronautTest + Testcontainers)

- **WebhookIntegrationTest** — fluxo completo (ENTRY→PARKED→EXIT→revenue), evento desconhecido, ENTRY duplicado, vaga ocupada, EXIT sem sessão
- **RevenueIntegrationTest** — revenue após fluxo completo retorna valor > 0; revenue sem sessões retorna 0
- **EdgeCaseIntegrationTest** — EXIT sem PARKED (422), placa formato inválido (400), event_type em branco (400)
- **TestClockFactory** — substitui o `Clock` real por um fixo (`@Replaces`) nos testes de integração, garantindo tempo determinístico

## Modelo de Dados

```sql
sector     (id, name, base_price, max_capacity, version)
spot       (id, sector_id FK, lat, lng, occupied, version)  -- UNIQUE(lat, lng)
parking_session (id, license_plate, sector_id FK, spot_id FK, entry_time,
                 parked_time, exit_time, price_at_entry, amount_charged, status, version)
```

**Índices:**
- `idx_session_plate_status` em `(license_plate, status)` — otimiza `findActiveByPlate`
- `idx_session_sector_exit` em `(sector_id, exit_time)` — otimiza queries de revenue
- `uq_spot_coordinates` UNIQUE em `(lat, lng)` — garante unicidade de coordenadas

Migration gerenciada pelo Flyway em `adapter-outbound/src/main/resources/db/migration/` (V1–V3).

## Testando com cURL

### Fluxo completo (ENTRY → PARKED → EXIT)

```bash
# 1. ENTRY
curl -s -X POST http://localhost:3003/webhook \
  -H "Content-Type: application/json" \
  -d '{"event_type":"ENTRY","license_plate":"ABC-1234","entry_time":"2025-01-01T10:00:00Z"}'

# 2. PARKED
curl -s -X POST http://localhost:3003/webhook \
  -H "Content-Type: application/json" \
  -d '{"event_type":"PARKED","license_plate":"ABC-1234","lat":-23.5505,"lng":-46.6333}'

# 3. EXIT
curl -s -X POST http://localhost:3003/webhook \
  -H "Content-Type: application/json" \
  -d '{"event_type":"EXIT","license_plate":"ABC-1234","exit_time":"2025-01-01T12:00:00Z"}'
```

### Testar race condition (ENTRY duplicado — segundo deve falhar com 422)

```bash
# Enviar dois ENTRYs simultâneos para a mesma placa
curl -s -X POST http://localhost:3003/webhook \
  -H "Content-Type: application/json" \
  -d '{"event_type":"ENTRY","license_plate":"DUP-9999","entry_time":"2025-01-01T10:00:00Z"}' &
curl -s -X POST http://localhost:3003/webhook \
  -H "Content-Type: application/json" \
  -d '{"event_type":"ENTRY","license_plate":"DUP-9999","entry_time":"2025-01-01T10:00:01Z"}' &
wait
```

### Consultar receita

```bash
curl -s "http://localhost:3003/revenue?date=2025-01-01&sector=A"
```
