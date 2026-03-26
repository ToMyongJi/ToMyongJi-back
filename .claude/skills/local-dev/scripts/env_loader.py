import sys
import json
from pathlib import Path

SENSITIVE_KEYS = {
    "DB_PASSWORD", "JWT_SECRET", "OCR_SECRET_KEY", "MAIL_PASSWORD",
}

def load_secrets():
    secrets = {}

    # 1. 루트 디렉토리의 .env 파일 로드
    env_path = Path(".env")
    if env_path.exists():
        for line in env_path.read_text(encoding="utf-8-sig").splitlines():
            line = line.strip()
            if line and not line.startswith("#"):
                key, _, value = line.partition("=")
                secrets[key.strip()] = value.strip()

    # 2. .claude/local-secrets.json 파일 로드
    json_path = Path(".claude/local-secrets.json")
    if json_path.exists():
        with open(json_path, "r", encoding="utf-8") as f:
            secrets.update(json.load(f))

    return secrets

if __name__ == "__main__":
    mask = "--mask" in sys.argv
    secrets = load_secrets()

    for k, v in secrets.items():
        display = "***" if (mask and k in SENSITIVE_KEYS) else v
        # PowerShell 세션에 환경변수를 주입하기 위한 명령어 출력
        print(f'$env:{k}="{display}"')