# 🛠️ Migration Execution Protocol

### 1. 현황 파악 (Pre-Check)
- `MySQL MCP`의 `describe_table` 도구를 사용하여 현재 테이블 구조를 확인한다.
- 변경하려는 필드에 기존 데이터가 있는지, 제약 조건(PK, FK, Index)은 무엇인지 파악한다.

### 2. 쿼리 실행 (Execute)
- `implementation_plan.md`에 작성된 SQL을 기반으로 쿼리를 실행한다.
- 복잡한 변경의 경우, 트랜잭션을 지원한다면 안전하게 실행하고 결과를 확인한다.

### 3. 검증 및 기록 (Verify & Record)
- 실행 후 다시 `describe_table`을 호출하여 의도한 대로 변경되었는지 확인한다.
- **Evidence 기록**: 아래 내용을 `.claude/evidence/{branch}.md`에 저장한다.
  - 실행한 SQL 쿼리문
  - 실행 전/후 `DESCRIBE` 결과 비교
  - 성공/실패 여부 및 발생한 경고 메시지