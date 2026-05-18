---
description: 현재 브랜치의 커밋, evidence, 세션 컨텍스트를 기반으로 GitHub PR을 자동 생성합니다.
usage: /pr
---

# 🚀 GitHub Pull Request Generator

커밋 이력과 `.claude/evidence/{branch}.md`를 기반으로 GitHub PR을 생성합니다.

## ⚠️ 사전 조건
- `git add`, `git commit`은 사용자가 이미 완료한 상태여야 합니다. 이 커맨드는 Push와 PR 생성만 수행합니다.

## 💡 실행 절차

### 1. 브랜치 및 이슈 번호 확인
```bash
git branch --show-current
```
브랜치명에서 이슈 번호를 추출한다 (예: `feat/107` → `107`, `fix/43` → `43`).

### 2. 데이터 수집 (병렬 실행)
다음 두 가지를 함께 읽는다:

**커밋 이력** — main 이후 현재 브랜치의 커밋 목록:
```bash
git log dev..HEAD --oneline
```

**Evidence 파일** — `.claude/evidence/{branch}.md` 읽기  
파일이 없으면 세션 컨텍스트(구현한 내용 기억)만으로 진행한다.

### 3. 본문 구성
`.claude/templates/pull_request.md`를 읽어 아래 기준으로 채운다:

- **연관 이슈**: 추출한 이슈 번호
- **작업 내용**: evidence의 구현 내용 + 커밋 메시지를 종합해 bullet 형식으로 정리. 세션에서 기억하는 주요 변경사항도 포함한다.
- **테스트 여부**: evidence에 단위테스트·API 테스트 결과가 있으면 반영, 없으면 기본값 유지
- **리뷰 요구사항**: 설계 변경·트레이드오프 등 evidence의 특이사항이 있으면 기재, 없으면 생략

### 4. 사용자 확인
PR 제목과 본문을 보여주고 승인을 요청한다. 수정 요청이 있으면 반영 후 재확인한다.

### 5. Push 및 PR 생성
승인 후 순서대로 실행한다:
```bash
git push origin {브랜치명}
```
이후 GitHub MCP로 PR을 생성한다:
- **owner**: `ToMyongJi`
- **repo**: `ToMyongJi-back`
- **base**: `dev`
- **head**: 현재 브랜치명

### 6. Jira 상태 전환
PR 생성 완료 후 evidence의 Jira 이슈 매핑 테이블에서 모든 하위 작업 키를 읽어 **CODE REVIEW**로 일괄 전환한다:
```bash
# jira 스킬 - 상태 전환 (transition id: 2) — 작업 키 목록 순회
```

### 7. Evidence 정리
Jira 전환이 완료된 후 아래 순서로 실행한다:

1. **Java 프로세스 종료** — 서버 로그 파일 점유를 해제한다:
```powershell
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2
```

2. **Evidence 파일 전체 삭제**:
```powershell
Get-ChildItem .claude/evidence/ -File | Remove-Item -Force
```

3. **dev 브랜치로 이동**:
```bash
git switch dev
```

삭제 후 "Evidence 정리 완료, dev 브랜치로 이동"을 출력한다.

## 📋 PR 제목 규칙
형식: `type(이슈번호): 제목` — 예: `feat(107): 대학 및 학생회 조회 API 구현`
