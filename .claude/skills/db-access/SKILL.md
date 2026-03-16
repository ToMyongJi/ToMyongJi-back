---
name: db-access
description: .env와 env_loader.py를 활용하여 안전하게 MySQL DB에 접속하고 데이터를 조회하는 가이드. MySQL MCP 없이 직접 CLI로 접근할 때 사용한다.
allowed-tools: Bash, Read
---

# 🗃️ DB Access Skill

이 스킬은 **데이터 조회 및 상태 확인**에만 사용합니다. 스키마 변경(DDL)이 필요한 경우 `database-migration` 스킬을 사용하세요.

## ⚠️ 사용 범위

| 이 스킬 사용 | `database-migration` 사용 |
|---|---|
| SELECT, SHOW, DESCRIBE | ALTER, CREATE, DROP |
| 현재 데이터 확인 | 스키마 변경 |
| 디버깅/검증 | 마이그레이션 실행 |

## 📋 접속 절차

### Step 1. SSH 터널 확인
DB는 EC2 Docker 컨테이너에 있으며, 로컬 3307 포트로 터널링됩니다.
터널이 열려 있지 않다면 먼저 실행하세요:
```
python .claude/skills/local-dev/scripts/ssh_tunnel.py
```

### Step 2. 크리덴셜 로드
`.env`에서 DB 접속 정보를 확인합니다:
```
python -X utf8 .claude/skills/local-dev/scripts/env_loader.py --mask
```
실제 값이 필요한 경우 `--mask` 없이 실행하되, 출력값을 로그에 남기지 마세요.

### Step 3. MySQL 접속
```bash
mysql -h 127.0.0.1 -P 3307 -u <DB_USERNAME> -p<DB_PASSWORD> tomyongjiTest
```

크리덴셜은 항상 `.env`에서 읽습니다. `application.properties`의 하드코딩된 값을 직접 복사하지 마세요.

## 🛡️ 보안 원칙
- 접속 명령어에 비밀번호를 평문으로 포함하는 경우 해당 명령어를 절대 로그/evidence에 기록하지 않습니다.
- 조회 결과에 개인정보가 포함된 경우 마스킹 후 기록합니다.
- 데이터 변경(INSERT/UPDATE/DELETE)은 이 스킬의 범위 밖입니다. 반드시 사용자에게 확인을 받으세요.
