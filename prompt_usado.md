# Estapar Parking Management

## O Problema

Teste técnico da Estapar. Backend para gerenciar estacionamento com controle de vagas, entrada/saída de veículos, preço dinâmico baseado em lotação e consulta de receita.

Um simulador Docker (`cfontes0estapar/garage-sim:1.0.0`) expõe a configuração da garagem via GET /garage e dispara eventos de veículos (ENTRY, PARKED, EXIT) via webhook para nosso backend na porta 3003.

## Stack e Arquitetura

Kotlin 2.1.x, Micronaut, MySQL, Java 21, Gradle Kotlin DSL.

Hexagonal com enforcement real via Gradle multi-module o domain não pode ter dependência de framework no build.gradle.kts, isso garante em tempo de compilação que a separação é respeitada. Se alguém tentar importar Micronaut no domain, o build quebra.

A razão de usar multi-module e não só pacotes: pacotes são convenção, módulos são enforcement. Num projeto com mais de uma pessoa ou que evolui com o tempo, convenção é violada silenciosamente.


## Decisões de Design

**Chain of Responsibility nos fluxos de ENTRY e EXIT.** A justificativa: cada fluxo é uma sequência ordenada de steps que podem rejeitar (short-circuit) ou enriquecer um contexto compartilhado. Hoje são 4 steps cada, mas se amanhã entrar blacklist de placas, horário de funcionamento ou prioridade PCD, é um handler novo sem tocar nos existentes Open/Closed concreto. No dispatcher de eventos não uso chain porque são 3 tipos fixos que não vão crescer, um when resolve.

**Object Calisthenics onde agrega valor.** Primitivos de negócio wrapped em value classes com @JvmInline type safety em compile-time com zero overhead em runtime. First-class collections para encapsular lógica de busca e seleção de vagas. Domain models ricos com mutação controlada via métodos de negócio usando backing properties a transição de estado é responsabilidade do próprio objeto, não de quem o manipula.

**Entidades JPA separadas dos domain models.** O domain model expressa comportamento de negócio. A entidade JPA é um detalhe de infraestrutura. Mappers explícitos no adapter-outbound fazem a tradução. Isso mantém o domain limpo e testável sem framework.

## Regras de Negócio

O preço dinâmico é calculado por setor no momento da entrada e gravado na session. A justificativa de gravar o preço na entrada: o enunciado diz explicitamente "na hora da entrada", e isso evita que mudanças de lotação entre entrada e saída alterem retroativamente o preço.

Quatro faixas de ocupação com ajustes progressivos de -10%, 0%, +10%, +25%. Cobrança na saída com 30 minutos de cortesia e arredondamento de horas para cima. Setor lotado rejeita novas entradas.

Na ENTRY o evento não traz setor o sistema atribui ao primeiro setor com disponibilidade. No PARKED vem lat/lng match exato com as coordenadas do spot. Revenue é query agregada sobre as sessions finalizadas, não precisa de tabela própria.

## Testes

Testes de comportamento com Kotest BehaviorSpec. O ponto importante: os testes do domain devem rodar sem Micronaut, sem banco, sem nada se precisar de framework pra testar o domain, a hexagonal falhou.

Cobrir os cenários que validam as regras de negócio: as 4 faixas de preço, a cortesia dos 30 minutos, o arredondamento, setor cheio rejeitando entrada, transições de estado inválidas no domain model. Testes de integração no bootstrap com @MicronautTest e Testcontainers.

## Infraestrutura

Docker Compose com MySQL 8, o app (multi-stage build) e o simulador. Flyway para migrations. App na porta 3003.