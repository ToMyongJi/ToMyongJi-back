---
description: Antigravity의 walkthrough.md를 기반으로 GitHub PR을 자동 생성합니다.
usage: /pr
---

# 🚀 GitHub Pull Request Generator

당신은 Antigravity(Architect)가 검토한 최종 결과물(`walkthrough.md`)을 바탕으로 GitHub Pull Request를 생성합니다.

## ⚠️ 실행 전 주의 사항
- **직접 커밋 금지**: `git add`나 `git commit`은 사용자가 이미 완료한 상태여야 합니다. 당신은 오직 Push와 PR 생성만 수행합니다.
- **출처 확인**: 반드시 루트 디렉토리의 `walkthrough.md`가 존재하는지 확인하세요.

## 💡 실행 절차

1. **변경 사항 푸시**: 
   - 현재 브랜치명을 확인(`git branch --show-current`)한 후 `git push origin [브랜치명]`을 실행합니다.

2. **데이터 추출 (Context)**: 
   - `walkthrough.md`를 읽고 '구현 요약', '테스트 결과', '개선 제안' 내용을 추출합니다.
   - 브랜치명에서 이슈 번호를 특정합니다 (예: `feat/107` -> `107`).

3. **본문 구성**: 
   - `.claude/templates/pull_request.md` 파일을 읽어 가이드라인에 맞게 추출한 데이터를 채워 넣습니다.

4. **사용자 확인**: 
   - 생성된 PR 제목과 본문 마크다운을 사용자에게 보여주고 승인을 요청합니다.

5. **PR 생성**: 
   - 사용자가 승인하면 GitHub MCP를 사용하여 `dev` 브랜치를 타겟으로 PR을 생성합니다.

## 📋 PR 제목 규칙
- **형식**: `type(이슈번호): 제목`
- **예시**: `feat(107): 전자결재 생성 및 처리 로직 구현`