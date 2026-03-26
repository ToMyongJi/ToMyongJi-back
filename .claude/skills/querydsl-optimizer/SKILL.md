---
name: querydsl-optimizer
description: QueryDSL 성능 최적화 및 N+1 문제 해결 스킬. 안티그래비티가 성능 리스크를 분석하고, 클로드 코드가 최적화 쿼리를 구현 및 검증한다.
---

# ⚡ QueryDSL Performance Optimization Skill

이 스킬은 복잡한 동적 쿼리나 대용량 데이터 조회 시 발생할 수 있는 쿼리 비용을 최소화하고, 유지보수가 용이한 QueryDSL 코드를 작성하기 위해 사용됩니다.

## 📋 에이전트별 역할

1. **Antigravity (Architect)**: 
   - 요구사항 분석 시 N+1 문제가 발생할 수 있는 연관관계를 식별합니다.
   - `implementation_plan.md`에 **Fetch Join 사용 여부**나 **No-Offset 페이징 전략** 등 최적화 설계안을 명시합니다.
2. **Claude Code (Developer)**: 
   - 안티그래비티의 설계에 따라 `BooleanExpression`을 활용한 모듈화된 쿼리를 구현합니다.
   - 구현 후 실제로 발생하는 쿼리 로그를 확인하여 의도한 대로 최적화되었는지 검증합니다.

## 🚫 중요 원칙
- **승인 우선**: 복잡한 조인이 포함된 쿼리는 반드시 `implementation_plan.md`에서 설계 승인을 받은 후 구현합니다.
- **가독성 보장**: 성능만큼 가독성도 중요합니다. 조건절은 반드시 `BooleanExpression` 메서드로 분리하여 명명합니다.