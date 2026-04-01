---
name: performance-profiler
description: 서비스 확장성 검증 및 성능 병목 지점 분석을 위한 전문 스킬. k6와 시스템 지표를 결합하여 정량적 데이터를 도출한다.
---

# 🚀 API Performance Profiler Skill

k6 부하 테스트와 EC2 시스템 지표를 결합하여 정량적 성능 데이터를 도출합니다.

## 📊 분석 핵심
수치 나열이 아닌 다음 인사이트에 집중합니다:
- **병목 지점**: CPU/Memory/Disk I/O/Network 중 가장 먼저 임계치에 도달하는 자원
- **RPS vs Latency**: RPS 증가에 따른 Latency 변화 곡선으로 안정적 수용 구간 도출
- **Saturation Point**: 서버가 무너지는 시점 및 현재 스펙 적정성
- **최적화 근거**: 성능 저하 원인 코드/쿼리를 식별하여 리팩토링의 정량적 근거 제시

## 🛠️ 운영 지침
- 테스트 전 `references/k6-template.js`를 읽고 프로젝트 문맥에 맞게 수정합니다.
- 결과는 항상 `.claude/evidence/`에 기록합니다.