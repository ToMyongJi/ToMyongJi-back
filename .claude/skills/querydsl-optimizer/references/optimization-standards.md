# 🏛️ QueryDSL Optimization Standards

### 1. N+1 문제 해결 전략
- **Fetch Join**: 단건 조회나 페이징이 없는 리스트 조회 시 `.fetchJoin()`을 사용하여 연관 엔티티를 한 번에 가져온다.
- **BatchSize**: 컬렉션(`OneToMany`) 페이징 조회 시 `fetchJoin()` 대신 `@BatchSize` 또는 `default_batch_fetch_size` 설정을 활용하여 IN 쿼리로 최적화한다.

### 2. 코드 재사용 (BooleanExpression)
- `where()` 절 내부의 로직은 반드시 `private BooleanExpression` 메서드로 추출한다.
- **장점**: 메서드 명을 통해 쿼리 의도를 명확히 전달하고, 다른 쿼리에서도 필터 로직을 재사용할 수 있다.

### 3. 대용량 페이징 최적화
- **Count 쿼리**: `PageableExecutionUtils`를 사용하여 카운트 쿼리가 불필요한 상황(첫 페이지인데 컨텐츠가 페이지 사이즈보다 적을 때 등)을 최적화한다.
- **No-Offset**: 데이터가 매우 많을 경우 `offset` 방식 대신 이전 페이지의 마지막 ID를 이용한 `where id < lastId` 방식을 검토한다.