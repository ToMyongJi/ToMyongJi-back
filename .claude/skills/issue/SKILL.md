---
name: issue
description: This skill should be used at the start of the /start workflow to create a GitHub issue and prepare the working branch. It collects issue info, creates the issue via GitHub MCP, switches to main, pulls latest, and checks out a new branch named after the issue number. If an issue number is passed as an argument (e.g. /start 340), it skips creation and uses the existing issue instead.
---

# Issue Skill

## 목적

작업 시작 전 GitHub 이슈를 생성하고 작업 브랜치를 준비한다.
이미 이슈가 존재하면 번호를 인자로 받아 생성을 건너뛴다.

## 분기

인자로 `issue_number`가 전달된 경우 → **절차 A: 기존 이슈 사용**
인자가 없는 경우 → **절차 B: 새 이슈 생성**

---

## 절차 A: 기존 이슈 사용

### A-1. 이슈 조회

GitHub MCP로 이슈 정보를 가져온다.

- **owner**: `ToMyongJi`
- **repo**: `ToMyongJi-back`
- **issue_number**: 전달받은 번호

제목 형식 `[{type}] {제목}`에서 type을 추출한다. (`feat` / `fix` / `refactor` / `docs`)

### A-2. 브랜치 준비 → [공통 절차](#공통-브랜치-준비)

### A-3. 결과 반환

```
이슈: #{issue_number} {제목}
브랜치: {type}/{issue_number}
작업 내용: (이슈 본문의 ✅ 작업할 내용 체크리스트)
```

---

## 절차 B: 새 이슈 생성

### B-1. 정보 수집

`implementation_plan.md`가 존재하면 읽어 다음 정보를 자동 추출한다:
- **제목**: 파일 최상단 `#` 헤더
- **타입**: 제목 또는 내용의 맥락에서 추론 (`feat` / `fix` / `refactor` / `docs`)
- **설명**: 파일 상단 설명 문단
- **작업 내용**: `## 작업 목록` 하위 체크박스 항목들

`implementation_plan.md`가 없는 경우에만 사용자에게 다음 정보를 한 번에 질문한다:

```
다음 정보를 알려주세요:
1. 타입: feat / fix / refactor / docs
2. 제목: 한 줄 요약 (예: "로그인 API 구현")
3. 설명: 무엇을 해야 하는지 간단히
4. 작업 내용: 체크리스트 항목들 (줄바꿈으로 구분)
5. 관련 자료: 디자인 링크, 관련 이슈/PR 번호 등 (없으면 생략)
```

### B-2. GitHub 이슈 생성

`.github/ISSUE_TEMPLATE/🫧투명지-issue🫧.md`를 읽어 본문 형식을 확인한 뒤, 수집한 정보를 채워 GitHub MCP로 이슈를 생성한다.

- **owner**: `ToMyongJi`
- **repo**: `ToMyongJi-back`
- **title**: `[{type}] {제목}`
- **body**: 템플릿 형식에 수집한 정보를 채운 내용

생성 후 반환된 `issue_number`를 확인한다.

### B-3. 브랜치 준비 → [공통 절차](#공통-브랜치-준비)

### B-4. 결과 반환

호출한 쪽(start)에서 이어서 쓸 수 있도록 다음 정보를 반환한다:

```
이슈: #{issue_number} [{type}] {제목}
브랜치: {type}/{issue_number}
작업 내용:
  - [ ] {작업1}
  - [ ] {작업2}
```

---

## 공통 브랜치 준비

절차 A-2 및 B-3에서 동일하게 실행한다. 브랜치명 규칙: `{type}/{issue_number}`

```bash
git switch dev
git pull origin dev
```

브랜치 존재 여부를 확인한 뒤 분기한다:
- **브랜치가 없으면**: `git switch -c {type}/{issue_number}`
- **브랜치가 이미 있으면**: `git switch {type}/{issue_number}`
