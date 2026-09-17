# Alerting 임계치 근거

기존 Alloy → Grafana Cloud 스택은 Alerting 규칙이 Grafana Cloud UI에서 수동으로만 만들어져
레포에 코드로 존재하지 않았고, 임계치를 왜 그 값으로 정했는지도 기록이 없었다. 이 문서는
`rules.yml`에 정의된 각 규칙의 임계치를 왜 그 값으로 정했는지 남긴다. 실측 데이터가 쌓이면
(특히 baseline 스냅샷 이후) 재조정할 것.

## RED

### `tomyongji-red-error-rate` — 5xx 에러율 > 5%, 5분 지속
- 절대적 SLO가 정의된 적이 없어서, 업계에서 흔히 쓰는 "5% 에러율"을 1차 기준선으로 잡았다.
- 5분 지속 조건을 둔 이유: 배포 직후 순간적인 헬스체크/재기동 노이즈로 인한 오탐을 줄이기 위함
  (docker-compose의 app healthcheck는 `start_period: 60s`, `retries: 15`로 이미 재시작 여유를 두고 있음).

### `tomyongji-red-latency-p99` — p99 응답시간 > 2초, 5분 지속
- `application.properties`에 새로 추가한 `management.metrics.distribution.slo.http.server.requests`의
  마지막 버킷(2s)과 값을 맞췄다. 즉 "SLO 버킷 중 가장 느슨한 버킷조차 넘는 요청이 p99를 지배하면 알림".
- 실제 트래픽/DB 응답 특성을 아직 몰라서 임의값에 가깝다 — baseline 스냅샷 확보 후
  p50/p95 실측치를 보고 좁힐 것 (성능 튜닝 작업의 첫 산출물이 이 값의 재조정이 되어야 함).

## USE

### `tomyongji-use-memory-critical` — 가용 메모리 < 100MiB, 2분 지속 (critical)
- dev EC2(t3.micro, 총 914MiB) 실측 기준 현재 available이 이미 209MiB까지 내려가 있고,
  과거 이 환경에서 OOM이 발생한 이력이 있다. 209MiB보다 낮은 100MiB를 critical 임계치로 잡아
  "이미 위험 구간에 진입했음"을 조기에 알리도록 했다.
- 2분으로 짧게 잡은 이유: OOM은 스왑까지 소진되면 수 초~수 분 내로 컨테이너가 죽으므로,
  긴 지속 조건을 걸면 알림이 무의미해짐.

### `tomyongji-use-swap-warning` — Swap 사용량 > 2GiB (전체 3GiB 중), 5분 지속
- 실측 기준 swap이 이미 1Gi/3Gi 사용 중. 여기서 추가로 1GiB가 더 늘어나 2GiB를 넘으면
  "메모리 부족이 스왑으로 흡수되고 있는 중" 신호로 보고 critical 이전 단계의 경고로 잡았다.

### `tomyongji-use-cpu-high` — CPU 사용률 > 90%, 10분 지속
- t3.micro는 버스트 가능 인스턴스(CPU 크레딧 기반)라 짧은 스파이크는 정상 동작이다.
  10분 지속 조건으로 "크레딧이 실제로 고갈되어가는 지속적 고사용" 상황만 걸러내도록 했다.

### `tomyongji-use-disk-high` — 디스크 사용률 > 80%, 10분 지속
- 기존 `.claude/commands/server-status.md`에 이미 있던 "디스크 사용률 80% 이상이면 경고" 관행을
  그대로 코드화했다 (기존 관행과의 일관성 유지).

### `tomyongji-use-hikari-pending` — HikariCP pending > 0, 2분 지속
- 커넥션 풀 자체가 t3.micro 메모리 제약 때문에 크지 않을 것으로 예상되어, "대기 스레드가
  하나라도, 잠깐이라도 발생하면" 바로 신호를 보도록 0을 임계치로 잡았다. 실측 후 풀 크기를
  키우거나 이 임계치를 완화할 수 있음.
  - 실제 HikariCP 풀 크기 설정을 확인하지 못했다 (application.properties에 별도 설정이 없으면
    Spring Boot 기본값 `spring.datasource.hikari.maximum-pool-size=10`이 적용됨) — 튜닝 작업
    시작 시 이 값을 baseline과 함께 반드시 확인할 것.

## 아직 안 한 것 / 알아야 할 제약
- Alertmanager 컨테이너를 추가하지 않고 Grafana 자체 Alerting(unified alerting)으로 처리했다 —
  t3.micro 메모리 여유가 없어서 컨테이너를 하나 더 늘리지 않기 위한 선택.
- `contact-points.yml`의 수신 주소는 placeholder다. 실제로 알림을 받으려면 사람이
  주소를 바꾸고 Grafana 컨테이너에 `GF_SMTP_*` 환경변수를 추가해야 한다 (앱이 쓰는 Gmail SMTP
  계정과는 별개로 Grafana 자체 SMTP 설정이 필요함). 이 PR은 "규칙 정의"까지만 다룬다.
