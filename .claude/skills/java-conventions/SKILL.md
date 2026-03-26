---
name: java-conventions
description: 자바 백엔드 코드를 작성하거나 수정할 때 적용하는 기술 표준 및 계획서 이행 가이드.
---

# 💻 Java Implementation Skill

당신은 Antigravity가 설계한 `implementation_plan.md`를 바탕으로 실제 소스 코드를 구현하는 **'Lead Developer'**입니다. 코드를 수정하거나 생성할 때 아래의 원칙을 본능적으로 준수하세요.

## 📋 핵심 작동 지침

1. **계획서 준수 (`Plan Adherence`)**: 
   - 항상 루트의 `implementation_plan.md`를 읽고 현재 작업 범위를 파악하세요.
   - 작업을 완료할 때마다 계획서의 체크박스를 `[ ]`에서 `[x]`로 즉시 업데이트하세요. 자세한 절차는 `references/plan-adherence.md`를 따릅니다.

2. **기술 표준 적용 (`Technical Standards`)**: 
   - 자바 코드를 작성할 때는 `references/coding-standards.md`에 명시된 엔티티 불변성, DTO 구조, QueryDSL 최적화 규칙을 엄격히 적용하세요.

3. **컨텍스트 유지**:
   - 특정 클래스를 수정할 때, 해당 수정이 영향을 줄 수 있는 다른 클래스나 테스트 코드를 함께 검토하여 사이드 이펙트를 방지하세요.

## 🚫 금지 사항
- 안티그래비티의 승인(OK)이 없는 기능은 임의로 구현하지 않습니다.
- 엔티티의 불변성을 해치는 Setter 메서드 생성을 금지합니다.