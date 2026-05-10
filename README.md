# FinSight — Financial Transaction Intelligence Platform

> Microservices backend project demonstrating Spring IoC, Java Stream API,  
> Native SQL Queries, and containerized deployment via Docker Compose.

---

## Overview

**FinSight** is a financial operations platform built for a banking/fintech context.  
It monitors payment transactions between accounts, detects suspicious activity patterns,  
and provides daily balance reconciliation with discrepancy reporting.

The system is split into two independently deployable microservices that communicate over HTTP:


---

## Technology Stack

| Layer           | Technology                              |
|-----------------|-----------------------------------------|
| Language        | Java 17                                 |
| Framework       | Spring Boot 3.2.5                       |
| Persistence     | Spring Data JPA + Hibernate 6           |
| Database        | PostgreSQL 16                           |
| Validation      | Jakarta Bean Validation                 |
| Documentation   | SpringDoc OpenAPI 3 / Swagger UI        |
| Build           | Maven 3.9                               |
| Containerization| Docker + Docker Compose                 |
| Utilities       | Lombok                                  |

---

## Quick Start — Run Everything with One Command

```bash
git clone <repo-url>
cd finsight
docker compose up --build
```

That's it. Docker Compose will:
1. Start PostgreSQL, create the database, run `init.sql` (schema + seed data)
2. Build and start `transaction-service` on port **8081**
3. Build and start `audit-service` on port **8082**

---

## Swagger UI

| Service             | Swagger URL                                      |
|---------------------|--------------------------------------------------|
| transaction-service | http://localhost:8081/swagger-ui.html            |
| audit-service       | http://localhost:8082/swagger-ui.html            |

---

## Stopping the Project

```bash
docker compose down          # stop containers
docker compose down -v       # stop + remove volumes (wipes DB data)
```

---

## Running Services Locally (without Docker)

Prerequisites: Java 17, Maven 3.9, PostgreSQL 16 running on localhost:5432

```bash
# 1. Create DB and run init.sql
psql -U postgres -c "CREATE DATABASE finsight;"
psql -U postgres -d finsight -f init.sql

# 2. Start transaction-service
cd transaction-service
mvn spring-boot:run

# 3. Start audit-service (in a new terminal)
cd audit-service
mvn spring-boot:run
```
