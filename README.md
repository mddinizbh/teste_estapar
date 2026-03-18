# Estapar Parking Management System

Sistema backend para gerenciamento de estacionamento com controle de vagas, entrada/saída de veículos, preço dinâmico e cálculo de receita.

## Stack

- **Kotlin 2.1.20** + **Java 21**
- **Micronaut 4.7.6** (HTTP server, Data JPA, HTTP client)
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
| **KSP** ao invés de KAPT | KAPT está deprecated no Kotlin 2.x; KSP é 2x mais rápido |
| **`io.micronaut.library`** nos adapters | Apenas bootstrap é a application; adapters são libraries |
| **`@Factory` no bootstrap** | Domain objects não têm anotações DI; a factory os registra como beans |
| **Pipeline handlers recebem ports via construtor** | Preserva boundary hexagonal — sem `@Inject` no domain |
| **`when()` simples** para dispatch de webhook | 3 tipos fixos não justificam Chain of Responsibility |

## Design Patterns

### Chain of Responsibility — Pipelines de ENTRY e EXIT

Cada pipeline é uma lista ordenada de handlers. Interface no domain:

```kotlin
interface PipelineHandler<T> {
    fun handle(context: T, next: (T) -> T): T
}
```

**Entry Pipeline:** CheckSectorCapacity → CalculateDynamicPrice → ReserveSpot → CreateSession

**Exit Pipeline:** FindActiveSession → CalculateCharge → ReleaseSpot → CloseSession

### Object Calisthenics

- **Primitivos encapsulados** em `@JvmInline value class` (LicensePlate, Money, SectorName, OccupancyRate)
- **Sem `var` público** nos domain models — mutação via métodos de negócio (`park()`, `exit()`)
- **Backing properties** para encapsular estado interno
- **Early return** e `when` exhaustivo ao invés de `else`

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

### Fluxo de Eventos

1. **ENTRY** → atribui setor com vagas → calcula preço dinâmico → reserva vaga → cria session
2. **PARKED** → encontra session ENTERED pela placa → associa spot por coordenadas exatas
3. **EXIT** → encontra session ativa → calcula cobrança → libera vaga → fecha session

## API

### Webhook (porta 3003)

```
POST http://localhost:3003/webhook
```

O simulador envia eventos ENTRY, PARKED e EXIT. Sempre retorna HTTP 200.

### Revenue

```
GET http://localhost:3003/revenue
Body: { "date": "2025-01-01", "sector": "A" }
Response: { "amount": 0.00, "currency": "BRL", "timestamp": "2025-01-01T12:00:00.000Z" }
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
- **Simulador** (`cfontes0estapar/garage-sim:1.0.0`)

### Desenvolvimento Local

1. Inicie o MySQL:
```bash
docker-compose up mysql
```

2. Compile e rode os testes:
```bash
./gradlew build
```

3. Rode apenas os testes de domain (sem banco):
```bash
./gradlew :domain:test
```

4. Inicie a aplicação:
```bash
./gradlew :bootstrap:run
```

## Estrutura de Testes

### Testes de Domain (Kotest BehaviorSpec, sem framework)

- **PricingServiceBehaviorTest** — 4 faixas de ocupação + 5 cenários de cobrança
- **ParkingSessionTest** — validações de transição de estado (ENTERED→PARKED→EXITED)
- **EntryPipelineTest** — setor cheio, setor com vagas, ocupação 60%
- **ExitPipelineTest** — veículo não encontrado, saída normal com cobrança

### Testes de Integração (@MicronautTest + Testcontainers)

- **WebhookIntegrationTest** — fluxo completo via HTTP com banco real

## Modelo de Dados

```sql
sector     (id, name, base_price, max_capacity)
spot       (id, sector_id FK, lat, lng, occupied)
parking_session (id, license_plate, sector_id FK, spot_id FK, entry_time,
                 parked_time, exit_time, price_at_entry, amount_charged, status)
```

Migration gerenciada pelo Flyway em `adapter-outbound/src/main/resources/db/migration/`.
