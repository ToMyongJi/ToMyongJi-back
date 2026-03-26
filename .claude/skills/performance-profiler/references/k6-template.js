/*
 * k6 Performance Test Template
 * * [Agent Note]
 * 부하 테스트 실행 전, 아래 'CONFIG' 객체의 값을 프로젝트 문맥에 맞게 수정하여 
 * '.claude/evidence/load-test.js'로 저장하십시오.
 */

const CONFIG = {
    TARGET_VU_START: 10,                  // 초기 가상 사용자 수
    TARGET_VU_MAX: 50,                    // 최대 가상 사용자 수
    API_URL: 'http://localhost:8080/api',  // 테스트 대상 API URL
    AUTH_TOKEN: 'sample-token'            // 인증 토큰 (필요시)
};

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: CONFIG.TARGET_VU_START },
        { duration: '1m', target: CONFIG.TARGET_VU_MAX },
        { duration: '30s', target: 0 },
    ],
};

export default function () {
    const url = CONFIG.API_URL;
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${CONFIG.AUTH_TOKEN}`,
        },
    };
    const res = http.get(url, params);
    check(res, { 'is status 200': (r) => r.status === 200 });
    sleep(1);
}