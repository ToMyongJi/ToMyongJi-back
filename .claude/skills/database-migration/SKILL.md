---
name: database-migration
description: 엔티티 변경에 따른 MySQL 스키마 동기화 스킬. 안티그래비티의 마이그레이션 설계를 클로드 코드가 실행하고 검증한다.
---

# 🗄️ Database Migration & Schema Sync Skill

이 스킬은 JPA 엔티티의 구조적 변경을 실제 MySQL 데이터베이스에 안전하게 반영하기 위해 사용됩니다. **Antigravity(설계)**와 **Claude Code(실행)**의 긴밀한 협업이 핵심입니다.

## 📋 에이전트별 역할

1. **Antigravity (Architect)**: 
   - 엔티티 변경 시 `implementation_plan.md`에 필요한 **DDL(ALTER, CREATE 등) 초안**을 작성합니다.
   - MySQL MCP를 통해 현재 테이블 구조를 미리 조회하여 설계에 반영합니다.
2. **Claude Code (Developer)**: 
   - `implementation_plan.md`의 마이그레이션 항목을 확인하고, `references/execution-steps.md`에 따라 실제 쿼리를 실행합니다.
   - 실행 전후의 스키마 상태를 비교하여 `.claude/evidence/`에 증거를 남깁니다.

## 🚫 중요 원칙
- 모든 마이그레이션 쿼리는 `implementation_plan.md`에 먼저 기록되어야 합니다.
- 데이터 유실 위험이 있는 작업(`DROP COLUMN`, `TRUNCATE` 등)은 실행 전 반드시 사용자에게 다시 확인을 요청합니다.