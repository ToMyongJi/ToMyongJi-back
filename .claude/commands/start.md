---
description: implementation_plan.md의 미완료 작업을 순차적으로 구현하고, Jira 타임라인을 동기화합니다.
usage: /start [issue_number]
---

# 🛠️ Implementation Start

`implementation_plan.md`에 정의된 계획을 순서대로 구현합니다.

## 📋 절차

### 0. 🚨 하드 게이트 — 반드시 첫 번째로 실행

**이 단계를 완료하기 전에 `implementation_plan.md`를 포함한 어떤 파일도 열지 않습니다.**

`git branch --show-current`로 현재 브랜치명을 확인하고, `.claude/evidence/{브랜치명}.md` 존재 여부만 확인합니다.

**파일이 존재하면** → 진행 중인 작업. `git pull origin {현재 브랜치명}`으로 최신화한 뒤 Step 1로 이동합니다.

**파일이 존재하지 않으면** → **즉시 `issue` 스킬을 호출합니다. 호출 전 다른 어떤 작업도 금지합니다.**
- `/start {issue_number}` 형태로 인자가 전달된 경우: 해당 번호를 `issue` 스킬에 전달합니다.
- 인자가 없는 경우: 번호 없이 `issue` 스킬을 호출합니다.

스킬 완료 후 반환된 **이슈 번호**, **브랜치명**, **작업 내용**을 이후 단계에서 사용합니다.

**이슈 스킬 완료 후** → Jira 이슈를 계획하고 생성합니다:

**0-J-1. Jira 계획 수립**: 아래 표를 작성하여 사용자에게 보여줍니다.

| 구분 | 제목 | 레이블 | 시작일 | 기한 |
|------|------|--------|--------|------|
| 에픽 | (plan 주제) | (판단) | 오늘 | 오는 목요일 |
| 작업 1 | (체크박스 항목) | (판단) | 오늘 | (에픽 기한 내 배분) |
| 작업 2 | ... | ... | (작업1 기한 다음날) | ... |

레이블은 `feat`, `ops`, `refact`, `test` 중 내용 기반으로 판단합니다.
하위 작업의 기한은 에픽 기한 안에서 작업 순서에 따라 균등 배분합니다.

**0-J-2. 사용자 승인**: 위 표를 보여주고 "이대로 생성할까요?" 확인을 받습니다.

**0-J-3. 생성**: 승인 후 `jira` 스킬로 에픽 → 작업 순으로 생성합니다.
생성된 키를 evidence 파일의 "Jira 이슈 매핑" 테이블에 기록합니다.

**0-J-4. 일괄 DEV IN PROGRESS 전환**: 생성된 모든 하위 작업(에픽 제외)을 즉시 **DEV IN PROGRESS**로 전환합니다.

### 1. Evidence 초기화 (최초 1회)

`.claude/evidence/{branch}.md`가 없으면 새로 생성합니다:
```markdown
# Evidence: {branch}
> 브랜치: {branch} | 시작일: {오늘 날짜}

## Jira 이슈 매핑
- 에픽: (생성 후 기입)
| 작업 | Jira 키 |
|------|---------|
```
파일이 이미 존재하면 초기화 없이 이어서 누적합니다.

### 2. 계획 스캔

`implementation_plan.md`에서 미완료(`[ ]`) 항목을 모두 찾습니다.

### 3. ADR 참고

`.claude/docs/ADR.md`를 열어 기존 결정 사항을 확인합니다.
현재 태스크와 관련된 항목이 있으면 해당 결정을 따릅니다.

### 4. 구현 (태스크별 반복)

가장 상단의 미완료 항목을 목표로 설정합니다.

태스크 텍스트에 `[Refactoring]` 태그가 포함되어 있는지 확인합니다.

---

#### 🔁 경로 A — 일반 태스크 (TDD 사이클)

`[Refactoring]` 태그가 **없는** 경우:

**4-A-1. Red: 실패 테스트 작성 (tdd-test-writer)**
`tdd-test-writer` 에이전트를 호출합니다. 전달 내용:
- 현재 태스크 항목의 전체 텍스트
- 관련 도메인/클래스 이름 (파악된 경우)

에이전트가 반환한 테스트 파일 경로와 테스트 목록을 확인합니다.

**4-A-2. Green: 구현 (tdd-impl-writer)**
`tdd-impl-writer` 에이전트를 호출합니다. 전달 내용:
- 4-A-1에서 작성된 테스트 파일 경로
- 현재 태스크 항목의 전체 텍스트

모든 테스트가 통과한 것을 확인한 후 다음 단계로 넘어갑니다.

---

#### ♻️ 경로 B — 리팩토링 태스크

`[Refactoring]` 태그가 **있는** 경우 tdd-test-writer를 건너뜁니다.

**4-B-1. 기존 테스트 확인**
관련 도메인/클래스의 기존 테스트 파일을 찾아 경로를 메모합니다.
기존 테스트가 없으면 사용자에게 알리고 계속할지 확인합니다.

**4-B-2. 리팩토링 구현 (tdd-impl-writer)**
`tdd-impl-writer` 에이전트를 호출합니다. 전달 내용:
- 현재 태스크 항목의 전체 텍스트 (`[Refactoring]` 태그 포함)
- 4-B-1에서 확인한 기존 테스트 파일 경로 (있는 경우)
- "이 태스크는 리팩토링입니다. 새 테스트를 작성하지 말고, 기존 동작을 유지하며 코드 구조만 개선하세요."

기존 테스트가 리팩토링 전후 모두 통과하는 것을 확인한 후 다음 단계로 넘어갑니다.

---

필요 시 `java-conventions`, `local-dev` 스킬을 활성화합니다.

### 5. 체크박스 즉시 업데이트 ← 구현 완료 직후 반드시 실행

해당 항목의 코드 구현이 끝나는 즉시 `implementation_plan.md`에서 `[ ]` → `[x]`로 변경합니다.
**API 테스트 결과를 기다리지 않습니다.** 다음 태스크로 넘어가기 전에 반드시 파일을 수정합니다.

### 6. ADR 기록 (해당 시)

구현 중 다음 중 하나에 해당하는 결정이 있었으면 `.claude/docs/ADR.md`에 추가합니다:
- 라이브러리/기술 선택 (QueryDSL vs JPQL, Batch vs 개별 저장 등)
- 성능 vs 가독성 등 명확한 트레이드오프가 있는 설계 결정
- 향후 변경 시 맥락 없이는 이유를 알 수 없는 구조적 선택

```markdown
### ADR-XXX: {결정 사항}
**결정**: {무엇을 선택했는지}
**이유**: {왜 선택했는지}
**트레이드오프**: {무엇을 포기했는지, 어떤 부작용을 감수했는지}
```

ADR 번호는 기존 마지막 번호에서 1 증가시킵니다. 사소한 구현 선택은 기록하지 않습니다.

### 7. Evidence 누적

```markdown
## ✅ {작업 제목} ({Jira 키})

**구현 내용**
- 수정/생성 파일 및 핵심 변경 사항

**특이사항**
- 설계 변경, 트레이드오프 등 (없으면 생략)
```

### 8. 연속 작업

사용자 확인을 받아 다음 미완료 항목으로 진행합니다.

### 9. API 테스트 — 모든 태스크 완료 후

모든 태스크 구현이 끝나면 아래 절차로 HTTP API를 검증합니다. 결과는 evidence에 누적됩니다.

**9-1. 로컬 서버 기동**
`local-dev` 스킬의 `references/server-protocol.md`를 따라 서버를 기동합니다 (SSH 터널 → 빌드 → 실행).
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
```

**9-2. 사전 데이터 준비 (필요 시)**
조회 API 테스트 전, 필요한 데이터를 `db-access` 스킬로 확인하거나 API를 통해 삽입합니다.

**9-3. 인증 토큰 발급 (필요 시)**
```powershell
$body = @{ loginId = "testUser"; password = "password123!" } | ConvertTo-Json
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
$res = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $bodyBytes -ContentType "application/json; charset=utf-8"
$token = $res.data.accessToken
```

**9-4. API 테스트**
`local-dev` 스킬의 `references/api-testing.md` 패턴으로 대상 엔드포인트를 호출합니다.
```powershell
$headers = @{ Authorization = "Bearer $token" }
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/<endpoint>" -Method Get -Headers $headers
$response | ConvertTo-Json
```
응답 코드, 응답 바디, 비즈니스 로직 결과를 검증합니다.

**9-5. Evidence 누적**
```markdown
**API 테스트**
- `{METHOD} /api/{endpoint}` → {status} ✅
  ```json
  { 핵심 응답 필드만 발췌 }
  ```
```

### 10. 커밋 메시지 제안 — API 테스트 통과 후

`git status --short`로 변경 파일(M)·신규 파일(??) 전체 목록을 확인합니다.
파일들을 **변경 성격**에 따라 1개 또는 N개 묶음으로 나누고, 각 묶음에 커밋 메시지를 제안합니다.

묶음 기준:
- **DTO/응답 변경**: 필드 추가·수정된 DTO, 관련 Projection 변경
- **도메인 로직·쿼리**: Repository/QueryRepository, Service 변경
- **API 레이어**: Controller 변경
- **테스트**: 테스트 파일 변경
- 성격이 동일하면 하나로 합칩니다. 파일 수가 적으면 단일 커밋도 무방합니다.

출력 형식:
```
**커밋 1** — {파일 목록}
{type}: {제목}

- {변경 내용 bullet}
```
커밋 타입은 프로젝트 컨벤션(`feat`, `fix`, `refactor`, `test`, `style`)을 따릅니다.

## 🚫 주의

- **evidence 파일이 없으면 `implementation_plan.md` 포함 어떤 파일도 읽지 않고 즉시 `issue` 스킬을 호출합니다.**
- 한 번에 **하나의 체크박스 항목**에만 집중합니다.
- **태스크 구현 완료 즉시** `implementation_plan.md` 체크박스를 업데이트합니다. 절대 나중으로 미루지 않습니다.
- 설계 변경이 필요한 상황이 발생하면 즉시 중단하고 사용자에게 보고합니다.
- Evidence는 Antigravity가 `walkthrough.md`를 작성하는 원본 자료입니다.
