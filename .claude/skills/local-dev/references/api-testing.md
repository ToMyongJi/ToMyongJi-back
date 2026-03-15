# 📡 API Verification Tips

이 문서는 PowerShell을 사용하여 로컬 서버의 REST API 기능을 검증하고, 그 결과를 에비던스로 남기는 표준 방법을 정의합니다.

## 1. PowerShell Invoke-RestMethod 활용
윈도우 환경에서 한글 인코딩 문제를 방지하고 복잡한 JSON 요청을 보내기 위해 아래 형식을 사용합니다.

```powershell
# JSON 바디 정의 및 UTF-8 바이트 배열 변환
$jsonBody = @{
    email = "admin@example.com"
    password = "password123!"
} | ConvertTo-Json

$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($jsonBody)

# API 호출 및 결과 출력
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/login" `
                             -Method Post `
                             -Body $bodyBytes `
                             -ContentType "application/json; charset=utf-8"

$response | ConvertTo-Json
---