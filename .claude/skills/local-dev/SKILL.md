---
name: local-dev
description: 윈도우 환경의 로컬 서버 제어, 빌드 및 보안(Secret) 관리. 서버 기동이 필요하거나 API를 테스트할 때 사용한다.
allowed-tools: Bash, Read, Edit, Write
---

# 🔐 Local Development & Security Skill

## 🛡️ 보안 원칙
1. **마스킹**: `.env`의 실제 값은 절대 출력하지 말고 `***`로 표시합니다.
2. **격리**: 모든 서버 로그와 실행 증거는 `.claude/evidence/`에 저장합니다.
3. **env_loader 원칙**: DB 접속 정보는 반드시 `env_loader.py`를 통해서만 읽습니다. `.env`가 단일 출처입니다. `application.properties`의 하드코딩된 값을 직접 사용하지 마세요.

## 📋 상황별 가이드라인
1. **서버 기동/빌드**: `references/server-protocol.md`를 읽고 절차를 따릅니다. **Step 0 (SSH 터널)을 반드시 먼저 실행**합니다.
2. **환경 변수 주입**: `references/secret-management.md`를 참조합니다.
3. **API 검증**: `references/api-testing.md`의 PowerShell 팁을 활용합니다.
4. **SSH 터널 단독 실행**: `python .claude/skills/local-dev/scripts/ssh_tunnel.py`