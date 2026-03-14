# 🔐 Secret Management Guide

이 문서는 로컬 개발 환경에서 사용되는 비밀번호, API 키 및 테스트 계정 정보를 안전하게 관리하고 주입하는 절차를 정의합니다.

## 1. 환경 변수 주입 (Environment Variable Injection)
PowerShell 세션에 `.env` 및 `local-secrets.json`의 값을 안전하게 주입하기 위해 `scripts/env_loader.py`를 호출합니다. 이 과정에서 실제 값은 터미널에 노출되지 않도록 마스킹 원칙을 준수합니다.

```powershell
# Python 로더 실행 및 반환된 명령어를 세션에 적용
$env_commands = python .claude/skills/local-dev/scripts/env_loader.py
Invoke-Expression ($env_commands -join "`n")

Write-Host "[Status] Environment variables loaded (Values masked)"
---