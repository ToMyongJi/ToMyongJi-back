---
name: java-conventions
description: 자바 백엔드 코드를 작성하거나 수정할 때 적용하는 기술 표준 및 계획서 이행 가이드.
---

# 💻 Java Implementation Skill

`implementation_plan.md` 기반으로 코드를 구현할 때 아래 원칙을 준수합니다.

## 📋 핵심 지침

1. **계획서 준수**: 루트의 `implementation_plan.md`를 읽고 작업 범위를 파악합니다. 작업 완료 시 체크박스를 즉시 `[x]`로 업데이트합니다. 절차는 `references/plan-adherence.md` 참조.

2. **기술 표준**: `references/coding-standards.md`에 명시된 엔티티 불변성, DTO 구조, QueryDSL 규칙을 엄격히 적용합니다.

3. **사이드 이펙트 방지**: 클래스 수정 시 영향받는 다른 클래스와 테스트 코드를 함께 검토합니다.

## 🚫 금지
- 안티그래비티의 승인 없는 기능 임의 구현 금지.
- 엔티티에 Setter 메서드 생성 금지.