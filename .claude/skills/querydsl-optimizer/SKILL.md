---
name: querydsl-optimizer
description: QueryDSL 성능 최적화 및 N+1 문제 해결 스킬. 안티그래비티가 성능 리스크를 분석하고, 클로드 코드가 최적화 쿼리를 구현 및 검증한다.
---

# ⚡ QueryDSL Performance Optimization Skill

동적 쿼리의 비용을 최소화하고 유지보수 가능한 QueryDSL 코드를 작성합니다. Antigravity가 `implementation_plan.md`에 Fetch Join/No-Offset 등 최적화 설계안을 명시하면, Claude Code가 구현 후 쿼리 로그로 검증합니다.

## 🚫 원칙
- 복잡한 조인 쿼리는 `implementation_plan.md`의 설계 승인 후 구현합니다.
- 조건절은 반드시 `BooleanExpression` 메서드로 분리합니다.
- 최적화 기준은 `references/optimization-standards.md`, 검증 절차는 `references/performance-check.md` 참조.