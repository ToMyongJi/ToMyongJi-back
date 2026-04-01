---
description: 부하 테스트(k6)를 실행하고 시스템 성능 지표를 분석합니다.
usage: /stress
---

# 🔥 Performance Stress Test

`performance-profiler` 스킬을 사용하여 시스템 한계를 테스트합니다.

## 📋 절차

1. **테스트 설계**: `.claude/skills/performance-profiler/references/k6-template.js`를 읽고 대상 엔드포인트와 VU를 설정하여 `.claude/evidence/load-test.js`를 생성합니다.

2. **부하 테스트 실행**: PowerShell에서 실행합니다.
   ```powershell
   k6 run --summary-export=.claude/evidence/k6-summary.json .claude/evidence/load-test.js
   ```

3. **서버 지표 수집**: 부하 실행 직후 EC2 자원 상태를 수집합니다.
   ```bash
   ssh -i "C:/Users/jinhy/.ssh/id_rsa" -o StrictHostKeyChecking=no ubuntu@3.38.75.240 \
     "docker stats --no-stream --format 'table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}'" \
     > .claude/evidence/docker-stats.log
   ```

4. **결과 분석**: 수집된 데이터를 분석하여 **병목 지점, RPS 대비 응답성, 개선 제안**을 포함한 보고서를 출력합니다.