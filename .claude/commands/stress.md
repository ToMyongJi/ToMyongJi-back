---
description: 부하 테스트(k6)를 실행하고 시스템 성능 지표를 분석합니다.
usage: /stress
---

# 🔥 Performance Stress Test

이 커맨드는 `performance-profiler` 스킬을 사용하여 시스템의 한계를 테스트합니다.

## 📋 실행 절차

1. **테스트 설계**:
   - `references/k6-template.js`를 읽고 현재 테스트 대상 엔드포인트와 목표 부하량(VU)을 설정하여 `.claude/evidence/load-test.js`를 생성합니다.

2. **부하 테스트 실행**:
   - PowerShell에서 `k6 run --summary-export=.claude/evidence/k6-summary.json .claude/evidence/load-test.js`를 실행합니다.

3. **지표 수집**:
   - 부하 실행 중 혹은 직후에 아래 명령을 Bash 툴로 실행하여 EC2 서버 자원 상태를 획득합니다.
   ```bash
   ssh -i "C:/Users/jinhy/.ssh/id_rsa" -o StrictHostKeyChecking=no ubuntu@3.38.75.240 \
     "docker stats --no-stream --format 'table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}'" \
     > .claude/evidence/docker-stats.log
   ```

4. **결과 분석 및 보고**:
   - `scripts/analyze-metrics.ps1`을 참고하여 수집된 데이터를 분석합니다.
   - **병목 지점, RPS 대비 응답성, 개선 제안**을 포함한 보고서를 작성하여 출력합니다.

## 🚫 주의 사항
- 로컬 환경에서 실행 시 다른 무거운 프로세스는 종료하세요.
- 테스트 후 생성된 대용량 로그 파일은 필요시 요약 후 삭제하여 저장 공간을 관리하세요.