# ⚠️ Database Safety Guidelines

- **Default Value**: `NOT NULL` 제약 조건을 추가할 때는 반드시 기존 데이터를 고려한 `DEFAULT` 값을 설정한다.
- **Index Check**: 대용량 테이블의 인덱스 추가/삭제 시 서비스 중단 가능성을 고려하여 설계 단계에서 검토한다.
- **Naming Convention**: 
  - Snake Case 사용 (예: `user_id`)
  - 제약 조건 이름 명시 (예: `fk_order_user`)
- **Rollback Plan**: 실패 시 복구할 수 있는 역방향 쿼리(Reverse SQL)를 항상 염두에 둔다.