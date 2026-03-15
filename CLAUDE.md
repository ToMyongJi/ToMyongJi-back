# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ToMyongJi** (`tomyongji`) is a Spring Boot backend for a student council budget transparency platform at Myongji University. It manages receipts, club memberships, and financial records with role-based access control.

## Build & Run Commands

```bash
# Build (skip tests)
./gradlew build -x test

# Run locally (requires MySQL on localhost:3307 and Redis on localhost:6379)
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.example.tomyongji.receipt.ReceiptServiceTest"

# Run a single test method
./gradlew test --tests "com.example.tomyongji.receipt.ReceiptServiceTest.methodName"

# Generate code coverage report
./gradlew test jacocoTestReport
```

Tests use H2 in-memory database. `ignoreFailures = true` is temporarily set in `build.gradle` (marked for deletion).

## Architecture

### Package Structure

```
src/main/java/com/example/tomyongji/
├── TomyongjiApplication.java
├── config/          # Spring beans: Security, Redis, CORS, Email, Swagger
├── domain/          # Feature domains (DDD-style)
│   ├── admin/       # Admin management
│   ├── auth/        # JWT authentication & email verification
│   ├── my/          # User profile & member management
│   ├── receipt/     # Receipt/ledger CRUD, OCR, CSV, PDF
│   └── status/      # Maintenance mode toggles
└── global/
    ├── common/response/ApiResponse.java   # Unified response wrapper for all endpoints
    └── error/                             # CustomException, ErrorMsg enum, GlobalExceptionHandler
```

Each domain follows: `controller/ → service/ → repository/ → entity/` with `dto/` and `mapper/` layers.

### Key Design Decisions

**Security:** JWT stateless auth via `JwtAuthenticationFilter`. Three roles: `STU` (student), `PRESIDENT`, `ADMIN`.

**Caching:** Redis-backed Spring Cache (Lettuce, 1-hour TTL). Cache stampede prevention via `sync=true`. Used primarily for paginated receipt queries.

**Performance:** Compound index `(student_club_id, date DESC, id DESC)` eliminates filesort. Full-Text Search with n-gram indexing for Korean text search.

**Receipt Upload Pipeline:** 4 sources: Tossbank PDF parsing → NAVER Cloud OCR → CSV batch upload → manual entry.

**Error Handling:** All exceptions go through `GlobalExceptionHandler` returning `ApiResponse`. Error codes are defined in `ErrorMsg` enum.

**Audit Logging:** MDC-based audit log (`AuditLog.java`) for sensitive update/delete operations.

### Database

MySQL on port 3307 (non-standard). Key entities: `User`, `StudentClub`, `College`, `Receipt`, `President`, `Member`, `EmailVerification`, `ClubVerification`, `MaintenanceConfig`. JPA `ddl-auto=update`.

### External Dependencies

- **NAVER Cloud OCR API** — receipt image-to-text
- **Gmail SMTP** — email verification
- **Apache PDFBox** — Tossbank PDF parsing
- **OpenCSV** — CSV export/import

## Infrastructure

- **CI/CD:** GitHub Actions (`.github/workflows/cicd.yml`) → Docker build/push → SSH deploy to AWS EC2 t3.micro
- **Environments:** `dev` (tomyongji-dev.site) and `prod` (api.tomyongji.com)
- **Monitoring:** Grafana + Loki + Prometheus via `config/config.alloy`
- **Docker:** `config/app.Dockerfile` + `config/docker-compose.yml`
- **Swap:** 3GB swap configured on EC2 to handle OOM on t3.micro
