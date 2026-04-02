---
description: Antigravity의 walkthrough.md를 기반으로 GitHub PR을 자동 생성합니다.
usage: /pr
---

# 🚀 GitHub Pull Request Generator

`walkthrough.md`를 기반으로 GitHub PR을 생성합니다.

## ⚠️ 사전 조건
- `git add`, `git commit`은 사용자가 이미 완료한 상태여야 합니다. 이 커맨드는 Push와 PR 생성만 수행합니다.
- 루트 디렉토리에 `walkthrough.md`가 존재해야 합니다.

## 💡 실행 절차

1. **Push**: 현재 브랜치 확인 후 `git push origin [브랜치명]` 실행.

2. **데이터 추출**: `walkthrough.md`에서 '구현 요약', '테스트 결과', '개선 제안'을 추출하고, 브랜치명에서 이슈 번호를 특정합니다 (예: `feat/107` → `107`).

3. **본문 구성**: `.claude/templates/pull_request.md`를 읽어 추출한 데이터로 채웁니다.

4. **사용자 확인**: PR 제목과 본문을 보여주고 승인을 요청합니다.

5. **PR 생성**: 승인 후 GitHub MCP로 `dev` 브랜치 타겟 PR을 생성합니다.

6. **일괄 CODE REVIEW 전환**: PR 생성 완료 후 evidence의 Jira 이슈 매핑 테이블에서 모든 하위 작업 키를 읽어 **CODE REVIEW**로 일괄 전환합니다.
```bash
# jira 스킬 - 상태 전환 (transition id: 2) — 작업 키 목록 순회
```

## 📋 PR 제목 규칙
형식: `type(이슈번호): 제목` — 예: `feat(107): 전자결재 생성 및 처리 로직 구현`