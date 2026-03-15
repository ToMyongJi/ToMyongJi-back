# k6 결과 파일과 docker stats 결과를 파싱하여 요약 리포트 생성
param (
    [string]$k6JsonPath,
    [string]$dockerStatsPath
)

Write-Host "`n--- Performance Analysis Summary ---" -ForegroundColor Cyan

if (Test-Path $k6JsonPath) {
    $k6Data = Get-Content $k6JsonPath | ConvertFrom-Json
    Write-Host "[k6] HTTP Req Duration (P95): $($k6Data.metrics.http_req_duration.p95) ms"
    Write-Host "[k6] Total Requests: $($k6Data.metrics.http_reqs.count)"
}

if (Test-Path $dockerStatsPath) {
    $dockerData = Get-Content $dockerStatsPath
    Write-Host "[Docker] Resource Usage captured."
    # 추가적인 문자열 파싱 로직을 클로드가 수행하도록 가이드
}