"""
SSH 터널 기동 스크립트
- .env 파일에서 SSH_KEY_PATH, DEV_SERVER_IP를 읽어 터널을 백그라운드로 실행합니다.
- 터널이 이미 활성화된 경우 재기동하지 않습니다.
- 실제 값은 마스킹하여 출력합니다.
"""
import os
import socket
import subprocess
import sys
from pathlib import Path


def load_env() -> dict:
    env_path = Path(".env")
    secrets = {}
    if not env_path.exists():
        print("[ERROR] .env 파일을 찾을 수 없습니다.", file=sys.stderr)
        sys.exit(1)
    for line in env_path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#"):
            key, _, value = line.partition("=")
            secrets[key.strip()] = value.strip()
    return secrets


def is_tunnel_active(port: int = 3307) -> bool:
    """로컬 포트가 이미 열려 있으면 터널이 활성화된 것으로 판단"""
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.settimeout(1)
        try:
            s.connect(("127.0.0.1", port))
            # MySQL 핸드쉐이크 바이트 수신 여부로 실제 연결 확인
            s.settimeout(2)
            data = s.recv(4)
            return len(data) > 0
        except (ConnectionRefusedError, TimeoutError, OSError):
            return False


def main():
    secrets = load_env()

    key_path = secrets.get("SSH_KEY_PATH")
    server_ip = secrets.get("DEV_SERVER_IP")

    if not key_path or not server_ip:
        print("[ERROR] .env에 SSH_KEY_PATH 또는 DEV_SERVER_IP가 없습니다.", file=sys.stderr)
        print("  예시: SSH_KEY_PATH=C:\\Users\\username\\.ssh\\id_rsa", file=sys.stderr)
        print("  예시: DEV_SERVER_IP=1.2.3.4", file=sys.stderr)
        sys.exit(1)

    if is_tunnel_active():
        print("[Status] SSH 터널이 이미 활성화되어 있습니다. (localhost:3307 → ***@***:3306)")
        return

    print(f"[Status] SSH 터널 기동 중... (key=***, server=***)")

    cmd = [
        "ssh",
        "-i", key_path,
        "-L", "3307:127.0.0.1:3306",
        "-N",          # 원격 명령 실행 없이 포워딩만
        "-o", "StrictHostKeyChecking=no",
        "-o", "ServerAliveInterval=60",
        f"ubuntu@{server_ip}",
    ]

    # 백그라운드 프로세스로 실행 (터미널 독립)
    subprocess.Popen(
        cmd,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
        creationflags=subprocess.CREATE_NO_WINDOW if sys.platform == "win32" else 0,
    )

    # 터널 활성화 대기 (최대 10초)
    import time
    for i in range(10):
        time.sleep(1)
        if is_tunnel_active():
            print("[Status] SSH 터널 연결 성공 ✓ (localhost:3307 → ***@***:3306)")
            return

    print("[ERROR] SSH 터널 연결 실패 — 키 경로 및 서버 IP를 확인하세요.", file=sys.stderr)
    sys.exit(1)


if __name__ == "__main__":
    main()
