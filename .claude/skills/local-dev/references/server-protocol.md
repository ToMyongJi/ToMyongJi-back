# 🛠️ Server Execution Protocol

이 문서는 윈도우(PowerShell) 환경에서 서버를 빌드하고, 아티팩트를 동기화하며, 백그라운드에서 안전하게 실행하기 위한 표준 절차를 정의합니다.

> **사전 조건**: 서버 기동 전 반드시 SSH 터널을 먼저 활성화해야 합니다.
> EC2 MySQL 컨테이너 접속이 없으면 JPA 초기화 단계에서 서버 기동이 실패합니다.

```powershell
# 0. SSH 터널 기동 (EC2 MySQL 연결)
# .env의 SSH_KEY_PATH, DEV_SERVER_IP를 읽어 로컬 3307 → EC2 MySQL 3306으로 터널링
python .claude/skills/local-dev/scripts/ssh_tunnel.py

# 1. 프로세스 정리 및 빌드 (Cleanup & Build)
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
.\gradlew.bat clean compileJava

# 2. 아티팩트 동기화 (Sync MapperImpl)
Get-ChildItem -Path "build\generated\sources\annotationProcessor\java\main" -Filter "*MapperImpl.java" -Recurse | ForEach-Object {
    $targetPath = $_.FullName.Replace("build\generated\sources\annotationProcessor\java\main", "src\main\generated")
    $targetDir = Split-Path $targetPath
    if (!(Test-Path $targetDir)) { New-Item -ItemType Directory -Path $targetDir -Force }
    Copy-Item -Path $_.FullName -Destination $targetPath -Force
}

# 3. 서버 백그라운드 기동 (Execution)
Start-Process -FilePath "java" -ArgumentList "-Dspring.profiles.active=local", "-jar", "build\libs\*.jar" -NoNewWindow -RedirectStandardOutput ".claude/evidence/server_startup.log" -RedirectStandardError ".claude/evidence/server_error.log"