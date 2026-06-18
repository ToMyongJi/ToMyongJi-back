# Architecture Decision Records

구현 중 내린 중요한 설계 결정을 기록합니다.
사소한 구현 선택은 기록하지 않고, 향후 변경 시 맥락 없이는 이유를 알 수 없는 결정만 남깁니다.

---

### ADR-001: Spring AI 버전을 1.0.0-M6 Milestone으로 고정
**결정**: `spring-ai-openai-spring-boot-starter:1.0.0-M6` + Spring Milestone 리포지토리 사용
**이유**: Spring AI 1.0.0 GA가 Maven Central에 미등록 상태. Spring release repo는 401 인증 요구. M6가 인증 없이 접근 가능한 최신 안정 버전.
**트레이드오프**: GA 대신 Milestone 버전 사용 → `ResponseFormat` 등 일부 API가 GA와 다름. 1.0.0 GA Maven Central 등록 후 버전 업그레이드 필요.

<!-- 새 ADR 형식:
### ADR-001: {결정 사항}
**결정**: {무엇을 선택했는지}
**이유**: {왜 선택했는지}
**트레이드오프**: {무엇을 포기했는지, 어떤 부작용을 감수했는지}
-->
