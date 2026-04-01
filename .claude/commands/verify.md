---
description: 수정/작성된 Service 클래스의 단위테스트를 실행하고, 로컬 서버를 띄워 HTTP API 테스트를 수행합니다. 결과를 evidence에 누적합니다.
usage: /verify
---

# ✅ Implementation Verify

구현이 완료된 항목에 대해 단위테스트와 HTTP API 테스트를 수행하고, 결과를 `.claude/evidence/{branch}.md`에 누적합니다.

## 📋 절차

### 1. 단위테스트 실행
수정/작성된 Service 클래스에 대응하는 테스트 클래스를 확인하고 실행합니다.
```powershell
.\gradlew.bat test --tests "com.example.tomyongji.<domain>.<ServiceClassName>Test"
```
실패 시 원인을 분석하고 수정한 후 재실행합니다. **단위테스트가 통과해야 다음 단계로 넘어갑니다.**

### 2. 로컬 서버 기동
`local-dev` 스킬의 `references/server-protocol.md`를 따라 서버를 기동합니다 (SSH 터널 → 빌드 → 실행).
서버 준비 확인:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
```

### 3. 사전 데이터 준비 (필요 시)
조회 API 테스트 전, 테스트에 필요한 데이터를 `db-access` 스킬로 확인하거나 API를 통해 삽입합니다.

### 4. 인증 토큰 발급 (필요 시)
인증이 필요한 API 테스트 전 로그인을 먼저 수행합니다.
```powershell
$body = @{ loginId = "testUser"; password = "password123!" } | ConvertTo-Json
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
$res = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $bodyBytes -ContentType "application/json; charset=utf-8"
$token = $res.data.accessToken
```

### 5. API 테스트
`local-dev` 스킬의 `references/api-testing.md` 패턴으로 대상 엔드포인트를 호출합니다.
```powershell
$headers = @{ Authorization = "Bearer $token" }
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/<endpoint>" -Method Get -Headers $headers
$response | ConvertTo-Json
```
응답 코드, 응답 바디, 비즈니스 로직 결과를 검증합니다.

### 6. Evidence 누적
테스트 결과를 `.claude/evidence/{branch}.md`에 아래 형식으로 **이어서 추가**합니다:

```markdown
**단위테스트**
- `{ServiceClassName}Test` — PASSED ({n}개)

**API 테스트**
- `{METHOD} /api/{endpoint}` → {status} ✅
  ```json
  { 핵심 응답 필드만 발췌 }
  ```
```

## 🚫 주의
- 단위테스트 실패 시 API 테스트로 넘어가지 않습니다.
- 서버 기동 전 SSH 터널이 열려있는지 반드시 확인합니다.
- 테스트용 데이터는 테스트 완료 후 정리 여부를 사용자에게 확인합니다.
- Evidence는 Antigravity가 `walkthrough.md`를 쓸 원본 자료이므로, 응답 전체가 아닌 **핵심 필드만** 발췌합니다.
