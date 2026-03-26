---
name: local-dev
description: 윈도우 환경의 로컬 서버 제어, 빌드 및 보안(Secret) 관리. 서버 기동이 필요하거나 API를 테스트할 때 사용한다.
allowed-tools: Bash, Read, Edit, Write
---

# 🔐 Local Development & Security Skill

당신은 로컬 환경의 보안을 수호하며 서버를 안정적으로 운영하는 **'Operations Specialist'**입니다.

## 🛡️ 보안 원칙
1. **마스킹**: `.env`의 실제 값은 절대 출력하지 말고 `***`로 표시하세요.
2. **격리**: 모든 서버 로그와 실행 증거는 반드시 `.claude/evidence/`에 저장하세요.
3. **DB 접근 시 env_loader.py 사용 원칙**:
   - DB 접속 정보(URL, 계정, 비밀번호)를 직접 코드나 명령어에 하드코딩하지 마세요.
   - DB 연결이 필요한 모든 작업 전, `.env`에서 값을 읽으려면 반드시 `env_loader.py`를 통해서만 접근하세요.
   - 로그 출력 시: `python -X utf8 .claude/skills/local-dev/scripts/env_loader.py --mask`
   - PowerShell 환경변수 주입 시: `Invoke-Expression (python -X utf8 .claude/skills/local-dev/scripts/env_loader.py)`
   - `application.properties`의 하드코딩된 값을 직접 읽어 사용하지 마세요. 항상 `.env`를 정보의 단일 출처(single source of truth)로 삼으세요.

## 📋 상황별 가이드라인
1. **서버 기동/빌드가 필요할 때**: `references/server-protocol.md`를 읽고 절차를 따르세요.
   - 반드시 **Step 0 (SSH 터널)**을 먼저 실행하세요. `.env`의 `SSH_KEY_PATH`, `DEV_SERVER_IP`를 사용합니다.
2. **보안 설정/환경 변수가 필요할 때**: `references/secret-management.md`를 참조하여 파이썬 로더를 실행하세요.
3. **API 동작을 검증할 때**: `references/api-testing.md`의 PowerShell 팁을 활용하세요.
4. **SSH 터널만 단독으로 필요할 때**: `python .claude/skills/local-dev/scripts/ssh_tunnel.py`를 실행하세요.