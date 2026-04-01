---
name: jira
description: .env의 Jira 크리덴셜로 이슈 조회·생성·상태 전환을 수행한다. start 커맨드에서 타임라인 동기화에 사용된다.
allowed-tools: Bash, Read
---

# 🎯 Jira Skill

`.env`의 `JIRA_BASE_URL`, `JIRA_EMAIL`, `JIRA_API_TOKEN`을 읽어 Jira REST API를 호출합니다.

## 🔑 크리덴셜 로드

```bash
export $(grep -E '^(JIRA_BASE_URL|JIRA_EMAIL|JIRA_API_TOKEN)' .env | xargs)
```

## 고정 값 (이 프로젝트)
- **프로젝트**: `KAN`
- **담당자 accountId**: `712020:9480b118-9a29-40fc-8d5f-0b284630ab02`
- **에픽 issuetype id**: `10002`
- **작업 issuetype id**: `10001`
- **레이블 선택지**: `feat`, `ops`, `refact`, `test`

## 📅 날짜 계산

```bash
# 오늘 날짜
TODAY=$(python -X utf8 -c "from datetime import date; print(date.today())")

# 오는 목요일 (당일이 목요일이면 다음 목요일)
NEXT_THU=$(python -X utf8 -c "
from datetime import date, timedelta
today = date.today()
days = (3 - today.weekday()) % 7
print(today + timedelta(days=days if days != 0 else 7))
")
```

## 📋 이슈 일괄 생성 (start 커맨드에서 사용)

에픽 생성 후 하위 작업들을 순서대로 생성합니다.

### 에픽 생성
```bash
EPIC_KEY=$(python -X utf8 -c "
import json, subprocess, os
payload = json.dumps({'fields': {
  'project': {'key': 'KAN'},
  'summary': '{에픽 제목}',
  'issuetype': {'id': '10002'},
  'assignee': {'accountId': '712020:9480b118-9a29-40fc-8d5f-0b284630ab02'},
  'labels': ['{레이블}'],
  'customfield_10015': '{시작일 YYYY-MM-DD}',
  'duedate': '{기한 YYYY-MM-DD}'
}})
result = subprocess.run(['curl','-s','-X','POST',
  '-u', os.environ['JIRA_EMAIL']+':'+os.environ['JIRA_API_TOKEN'],
  '-H','Content-Type: application/json',
  '--data-binary', payload,
  os.environ['JIRA_BASE_URL']+'/rest/api/3/issue'],
  capture_output=True, text=True)
print(json.loads(result.stdout)['key'])
")
echo "에픽: $EPIC_KEY"
```

### 하위 작업 생성 (에픽 연결 포함)
```bash
TASK_KEY=$(python -X utf8 -c "
import json, subprocess, os
payload = json.dumps({'fields': {
  'project': {'key': 'KAN'},
  'summary': '{작업 제목}',
  'issuetype': {'id': '10001'},
  'parent': {'key': '$EPIC_KEY'},
  'assignee': {'accountId': '712020:9480b118-9a29-40fc-8d5f-0b284630ab02'},
  'labels': ['{레이블}'],
  'customfield_10015': '{시작일 YYYY-MM-DD}',
  'duedate': '{기한 YYYY-MM-DD}'
}})
result = subprocess.run(['curl','-s','-X','POST',
  '-u', os.environ['JIRA_EMAIL']+':'+os.environ['JIRA_API_TOKEN'],
  '-H','Content-Type: application/json',
  '--data-binary', payload,
  os.environ['JIRA_BASE_URL']+'/rest/api/3/issue'],
  capture_output=True, text=True)
print(json.loads(result.stdout)['key'])
")
echo "작업: $TASK_KEY"
```

### 기한 균등 배분 계산
```python
from datetime import date, timedelta

start = date.today()
due = date(YYYY, MM, DD)  # 에픽 기한
tasks = ['작업1', '작업2', '작업3']  # 미완료 항목 목록
total_days = (due - start).days or 1

for i, task in enumerate(tasks):
    task_due = start + timedelta(days=round((i + 1) * total_days / len(tasks)))
    task_start = start if i == 0 else (start + timedelta(days=round(i * total_days / len(tasks))))
    print(f"{task}: {task_start} ~ {task_due}")
```

## 🔄 상태 전환

작업 시작/완료 시 Jira 이슈 상태를 변경합니다.

| 시점 | 전환 이름 | transition id |
|------|-----------|--------------|
| 작업 시작 시 | DEV IN PROGRESS | `5` |
| 커밋 완료 후 | CODE REVIEW | `2` |

```bash
# 상태 전환 실행
curl -s -X POST \
  -u "$JIRA_EMAIL:$JIRA_API_TOKEN" \
  -H "Content-Type: application/json" \
  --data-binary '{"transition":{"id":"{transition_id}"}}' \
  "$JIRA_BASE_URL/rest/api/3/issue/{ISSUE_KEY}/transitions"
```

## 🔍 기타 조회

### 이슈 조회
```bash
curl -s -u "$JIRA_EMAIL:$JIRA_API_TOKEN" \
  "$JIRA_BASE_URL/rest/api/3/issue/{ISSUE_KEY}" | python -X utf8 -m json.tool
```

### JQL 검색
```bash
curl -s -X POST \
  -u "$JIRA_EMAIL:$JIRA_API_TOKEN" \
  -H "Content-Type: application/json" \
  --data-binary '{"jql":"{JQL}","fields":["summary","status","assignee"],"maxResults":20}' \
  "$JIRA_BASE_URL/rest/api/3/search/jql" | python -X utf8 -m json.tool
```

## 🛡️ 보안 원칙
- `JIRA_API_TOKEN` 값을 로그나 evidence에 절대 기록하지 않습니다.
- 이슈 생성 실패 시 에러 응답을 확인하고 재시도합니다.
