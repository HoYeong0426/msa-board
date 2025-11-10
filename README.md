# MSA Board - 마이크로서비스 아키텍처 기반 게시판 시스템

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen)
![Gradle](https://img.shields.io/badge/Gradle-8.0-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Redis](https://img.shields.io/badge/Redis-7.4-red)
![Kafka](https://img.shields.io/badge/Kafka-3.2-black)

**이벤트 기반 아키텍처와 Outbox Pattern을 활용한 분산 시스템 프로젝트**

</div>

## 프로젝트 소개

MSA Board는 마이크로서비스 아키텍처를 기반으로 한 게시판 시스템입니다. 각 서비스는 독립적으로 배포 가능하며, 이벤트 기반 통신을 사용합니다.

### 핵심 특징

- **마이크로서비스 아키텍처**: 독립적인 서비스로 구성
- **이벤트 기반 통신**: Kafka를 통한 비동기 메시징
- **Outbox Pattern**: 트랜잭션 일관성 보장
- **CQRS 패턴**: 읽기/쓰기 분리
- **Redis 기반 샤딩**: 분산 환경에서의 작업 분산
- **Redis 캐싱**: 조회 성능 최적화

---

## 아키텍처 개요

### 시스템 전체 구조

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client (API Gateway)                    │
└────────────────────────────┬────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐
│   Command      │  │    Command      │  │    Command     │
│   Services     │  │    Services     │  │    Services    │
│                │  │                 │  │                │
│  Article       │  │   Comment       │  │     Like       │
└───────┬────────┘  └────────┬────────┘  └───────┬────────┘
        │                    │                   │
        │  ┌─────────────────┼─────────────────┐ │
        │  │   MySQL DB      │                 │ │
        │  │  (article,      │                 │ │
        │  │   comment,      │                 │ │
        │  │   like)         │                 │ │
        │  └───────────────────────────────────┘ │
        │                                        │
        │  ┌───────────────────────────────────┐ │
        │  │      Outbox Pattern               │ │
        │  │  ┌──────────┐                     │ │
        │  │  │ Outbox   │ ──┐                 │ │
        │  │  │ Table    │   │                 │ │
        │  │  └──────────┘   │                 │ │
        │  │                 │                 │ │
        │  │  ┌──────────┐   │                 │ │
        │  │  │Message   │   │                 │ │
        │  │  │Relay     │◄──┘                 │ │
        │  │  └────┬─────┘                     │ │
        │  └───────┼───────────────────────────┘ │
        │          │                             │
        └──────────┼─────────────────────────────┘
                   │
            ┌──────▼──────┐
            │   Kafka     │
            │   Topics    │
            │             │
            │ - board-article │
            │ - board-comment │
            │ - board-like    │
            │ - board-view    │
            └──────┬──────┘
                   │
    ┌──────────────┼──────────────┐
    │              │              │
┌───▼──────┐  ┌───▼──────┐  ┌───▼──────┐
│  Query   │  │  Query   │  │  Query   │
│ Services │  │ Services │  │ Services │
│          │  │          │  │          │
│ Article  │  │   Hot    │  │   View   │
│  Read    │  │ Article  │  │          │
└───┬──────┘  └───┬──────┘  └───┬──────┘
    │             │             │
    └─────────────┼─────────────┘
                  │
         ┌────────▼────────┐
         │      Redis      │
         │  - Cache        │
         │  - Sharding     │
         │  - Ranking      │
         └─────────────────┘
```

---

## 기술 스택

### Backend
- **Java 21**: 최신 LTS 버전
- **Spring Boot 3.3.2**: 마이크로서비스 프레임워크
- **Spring Data JPA**: 데이터 접근 계층
- **Spring Kafka**: 메시징
- **Spring Data Redis**: 캐싱 및 분산 조정

### Database & Cache
- **MySQL 8.0**: 관계형 데이터베이스
- **Redis 7.4**: 캐싱 및 분산 조정

### Message Queue
- **Apache Kafka**: 이벤트 스트리밍 플랫폼

## 주요 기능

### 1. 게시글 관리
- 게시글 CRUD 작업
- 페이지네이션 및 무한 스크롤 지원
- 게시판별 게시글 수 집계

### 2. 댓글 시스템
- 댓글 작성/삭제
- 무한 댓글 구조 (대댓글 지원)
- 게시글별 댓글 수 집계

### 3. 좋아요 기능
- 좋아요/좋아요 취소
- 게시글별 좋아요 수 집계
- 낙관적 락을 통한 동시성 제어

### 4. 조회수 집계
- 게시글 조회수 카운팅
- 중복 조회 방지

### 5. 인기 게시글
- 실시간 인기 게시글 계산
- 조회수, 댓글수, 좋아요수 기반 점수 계산
- Redis Sorted Set을 활용한 랭킹

### 6. 통합 조회
- 여러 서비스 데이터 통합 조회
- Redis 캐싱을 통한 성능 최적화
- 이벤트 기반 데이터 동기화

---

## 핵심 아키텍처 패턴

### 1. Outbox Pattern

트랜잭션 일관성을 보장하기 위해 Outbox Pattern을 구현했습니다.

```
┌─────────────┐
│   Service   │
│  (Article)  │
└──────┬──────┘
       │
       │ 1. 트랜잭션 내부
       │    Outbox 저장
       ▼
┌─────────────┐
│   Outbox    │
│   Table     │
└──────┬──────┘
       │
       │ 2. 트랜잭션 커밋 후
       │    Kafka 발행
       ▼
┌─────────────┐
│   Kafka     │
│   Topic     │
└─────────────┘
```

### 2. 분산 샤딩 (Distributed Sharding)

Redis 기반 샤드 할당으로 여러 인스턴스 간 작업을 분산합니다.

- **하트비트 메커니즘**: 3초마다 Redis에 생존 신호 전송
- **자동 샤드 할당**: 인스턴스 수에 따라 샤드 자동 분배
- **장애 복구**: 죽은 인스턴스 자동 감지 및 재할당

### 3. CQRS (Command Query Responsibility Segregation)

쓰기와 읽기를 분리하여 성능을 최적화했습니다.

- **Command Side**: 게시글/댓글/좋아요 서비스 (쓰기)
- **Query Side**: 게시글 서비스 (읽기)
- **이벤트 동기화**: Kafka를 통한 데이터 동기화

### 4. 이벤트 기반 아키텍처

서비스 간 낮은 결합을 위해 이벤트 기반 통신을 사용합니다.

---

## Common 모듈 상세

### 1. Snowflake ID Generator
분산 환경에서 고유 ID를 생성하는 Snowflake 알고리즘 구현

### 2. Event Module
이벤트 타입 정의 및 페이로드 클래스

### 3. Data Serializer
JSON 직렬화/역직렬화 유틸리티

### 4. Outbox Message Relay
- **OutboxEventPublisher**: 이벤트를 Outbox 테이블에 저장
- **MessageRelay**: 트랜잭션 후 Kafka로 발행
- **MessageRelayCoordinator**: Redis 기반 분산 샤딩

---

## 성능 최적화

- **Redis 캐싱**: 조회 성능 향상
- **비동기 처리**: Kafka를 통한 비동기 이벤트 처리
- **페이지네이션**: 대량 데이터 조회 최적화
- **인덱싱**: 데이터베이스 쿼리 성능 향상

---


<div align="center">


</div>
