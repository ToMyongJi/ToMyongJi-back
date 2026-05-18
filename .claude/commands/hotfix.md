---
description: 버그를 자연어로 설명받아 이슈 생성 → 브랜치 → 최소 수정 → 검증 → 커밋까지 처리합니다.
usage: /hotfix [issue_number]
---

# Hotfix

`implementation_plan.md` 없이 버그를 빠르게 수정합니다.  
서버 로그, 에러 메시지, 자연어 설명 등 어떤 형태의 버그 정보도 입력으로 받습니다.

## 절차

### 1. 이슈 준비

**인자로 `issue_number`가 전달된 경우** → GitHub MCP로 이슈를 조회하고 브랜치를 준비합니다.

```bash
git switch dev
git pull origin dev
git switch -c fix/{issue_number}
```

**인자가 없는 경우** → 대화 이력에서 버그 정보를 먼저 추출합니다:

- 에러 메시지, 스택 트레이스, 자연어 버그 설명 등 관련 내용을 현재 대화에서 찾습니다.
- 충분한 정보가 있으면 제목·설명·작업 내용을 자동 작성한 뒤 사용자에게 **확인**만 요청합니다:

  ```
  다음 내용으로 이슈를 생성할게요. 수정할 부분이 있으면 알려주세요:
  - 제목: {추출한 제목}
  - 설명: {추출한 설명}
  - 작업 내용: {추출한 체크리스트}
  ```

- 대화 이력에 버그 정보가 없거나 부족한 경우에만 다음을 질문합니다:

  ```
  다음 정보를 알려주세요:
  1. 제목: 한 줄 요약 (예: "사용자 삭제 시 FK 제약 조건 오류")
  2. 설명: 어떤 상황에서 어떤 에러가 발생하는지
  3. 작업 내용: 수정할 내용 체크리스트 (줄바꿈으로 구분, 없으면 설명에서 추론)
  ```

수집한 정보로 GitHub MCP에 이슈를 생성합니다:
- **owner**: `ToMyongJi`
- **repo**: `ToMyongJi-back`
- **title**: `[fix] {제목}`
- **body**: `.github/ISSUE_TEMPLATE/🫧투명지-issue🫧.md` 형식에 맞춰 작성

생성된 `issue_number`로 브랜치를 준비합니다:

```bash
git switch dev
git pull origin dev
git switch -c fix/{issue_number}
```

---

### 2. 버그 분석

버그 설명(에러 메시지, 스택 트레이스, 자연어)을 분석해 원인이 되는 코드를 찾습니다.

- 에러 메시지가 있으면 클래스명·메서드명·테이블명으로 Grep합니다.
- 원인 파일과 라인을 특정한 뒤, 수정 범위를 1~3줄 요약합니다.
- 수정 전 사용자에게 원인과 수정 방향을 한 문장으로 보고합니다.

---

### 3. 최소 수정

원인 코드만 수정합니다. 다음 원칙을 반드시 지킵니다:

- 수정 대상이 아닌 인접 코드·주석·포맷은 건드리지 않습니다.
- 버그 수정과 무관한 리팩토링은 하지 않습니다.
- 수정 후 변경 파일 목록(`git diff --name-only`)을 확인합니다.

---

### 4. 검증

**4-1. 컴파일 확인**

```powershell
.\gradlew.bat compileJava 2>&1 | Select-Object -Last 10
```

컴파일 에러가 있으면 즉시 수정 후 재확인합니다.

**4-2. 관련 테스트 실행**

수정된 클래스가 속한 도메인의 `*ServiceTest`를 찾아 실행합니다:

```powershell
.\gradlew.bat test --tests "com.example.tomyongji.<domain>.*ServiceTest" 2>&1 | Select-Object -Last 20
```

테스트가 없으면 해당 사실을 사용자에게 알리고 계속합니다.
테스트 실패 시 원인을 분석하고 수정 후 재실행합니다.

---

### 5. Evidence 기록

`.claude/evidence/fix-{issue_number}.md`를 생성합니다:

```markdown
# Hotfix Evidence: fix/{issue_number}
> 브랜치: fix/{issue_number} | 날짜: {오늘 날짜}

## 버그
{에러 메시지 또는 현상 요약}

## 원인
{원인 파일:라인 — 한 줄 설명}

## 수정
{수정 내용 — 변경된 코드 핵심만}

## 검증
- 컴파일: ✅
- 테스트: {통과한 테스트 클래스} ✅ / 테스트 없음
```

---

### 6. 커밋 메시지 제안

`git diff --name-only`로 변경 파일을 확인하고 아래 형식으로 커밋 메시지를 **제안**합니다.  
커밋은 직접 실행하지 않고 사용자가 확인 후 직접 실행합니다.

```
fix: {버그를 한 줄로 요약}

- {핵심 변경 내용 bullet}
```

커밋 타입은 항상 `fix`입니다.

---

## 주의

- `implementation_plan.md`는 생성하거나 읽지 않습니다.
- TDD 사이클(tdd-test-writer / tdd-impl-writer)은 사용하지 않습니다.
- 수정 범위가 예상보다 넓어지면 즉시 멈추고 사용자에게 보고합니다.
- 스키마 변경이 필요한 경우 `database-migration` 스킬을 활성화합니다.
