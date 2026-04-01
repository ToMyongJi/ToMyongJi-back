---
name: database-migration
description: 엔티티 변경에 따른 MySQL 스키마 동기화 스킬. 안티그래비티의 마이그레이션 설계를 클로드 코드가 실행하고 검증한다.
---

# 🗄️ Database Migration & Schema Sync Skill

JPA 엔티티 변경을 MySQL에 안전하게 반영합니다. Antigravity가 `implementation_plan.md`에 DDL 초안을 작성하면, Claude Code가 `references/execution-steps.md`에 따라 실행하고 `.claude/evidence/`에 전후 비교 증거를 남깁니다.

## 🚫 원칙
- 모든 마이그레이션 쿼리는 `implementation_plan.md`에 먼저 기록되어야 합니다.
- `DROP COLUMN`, `TRUNCATE` 등 데이터 유실 위험 작업은 실행 전 사용자에게 재확인합니다.
- 안전 지침은 `references/safety-guidelines.md` 참조.