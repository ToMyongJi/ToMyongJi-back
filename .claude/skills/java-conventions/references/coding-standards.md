# 🏛️ Java Spring Boot Coding Standards

### 1. Entity (엔티티)
- **생성 전략**: public 생성자 + `@Builder` 사용. 검증 로직이 복잡할 경우에만 정적 팩터리 메서드(`of`, `from`)를 사용하고 생성자 접근을 제한한다.
- **JPA 기본 생성자**: `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수 적용.
- **불변성**: Setter 사용 금지. 컬렉션은 방어적 복사 또는 일급 컬렉션으로 관리한다.

### 2. DTO (Data Transfer Object)
- **문서화**: 모든 필드에 `@Schema(description = "명확한 한국어 설명", example = "예시값")` 필수 작성.
- **계층 분리**: Request와 Response DTO를 목적에 맞게 엄격히 분리한다.

### 3. Query & DB
- **성능**: N+1 방지를 위해 Fetch Join 또는 EntityGraph를 최우선으로 고려한다.
- **QueryDSL**: 복잡한 쿼리는 `QueryRepository`로 분리하며, `BooleanExpression`을 반환하는 메서드로 필터 로직을 재사용한다.

### 4. 테스트 코드
- **Pairing**: 소스 수정 시 대응하는 테스트 코드도 반드시 업데이트한다.
- **경계값 검증**: `@ParameterizedTest`를 활용하여 비즈니스 로직의 임계점을 철저히 검증한다.