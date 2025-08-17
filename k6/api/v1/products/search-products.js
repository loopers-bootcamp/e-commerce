import http from 'k6/http';
import {check, sleep} from 'k6';
import {Trend} from 'k6/metrics';
import {URLSearchParams} from 'https://jslib.k6.io/url/1.0.0/index.js';

// ======== 환경 변수(기본값 포함) ========
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const BRAND_ID = Number(__ENV.BRAND_ID || 25);    // 1 ~ 50
const PAGES = Number(__ENV.PAGES || 100);         // 몇 페이지까지 측정할지
const START_PAGE = Number(__ENV.START_PAGE || 0); // 페이지 시작값 (0 또는 1)
const SIZE = Number(__ENV.SIZE || 20);            // 페이지 크기
const SORT = __ENV.SORT || 'POPULAR';             // LATEST | POPULAR | CHEAP
const KEYWORD = __ENV.KEYWORD || '';              // 없으면 전체
const REPEAT = Number(__ENV.REPEAT || 2);         // 각 페이지를 몇 번 반복 측정할지(평균용)
const SLEEP_SEC = Number(__ENV.SLEEP_SEC || 0.1); // 요청 간 간격(부하 조절)

// ======== 옵션 ========
// 페이지별로 순차 측정(노이즈 최소화). 필요시 VUs 늘려도 되지만 페이지별 비교엔 1이 깔끔.
export const options = {
    vus: 1,
    iterations: PAGES * REPEAT,
};

// 페이지별 레이턴시 트렌드(태그로 page를 붙여 나중에 필터링 가능)
const latencyByPage = new Trend('latency_by_page', true);

// ======== 유틸 ========
function buildUrl({brandId, page, size, sort, keyword}) {
    const params = new URLSearchParams();
    params.set('brandId', String(brandId));
    params.set('page', String(page));
    params.set('size', String(size));
    params.set('sort', sort);
    if (keyword && keyword.length > 0) params.set('keyword', keyword);

    // 실제 엔드포인트에 맞게 path 조정
    return `${BASE_URL}/api/v1/products?${params.toString()}`;
}

// ======== 테스트 본문 ========
export default function () {
    // __ITER: 0..(iterations-1)
    // 같은 페이지를 REPEAT번씩 측정하도록 매핑
    const pass = Math.floor(__ITER / PAGES); // 0..REPEAT-1 (참고용)
    const pageIdx = __ITER % PAGES;
    const page = START_PAGE + pageIdx;

    const url = buildUrl({
        brandId: BRAND_ID,
        page,
        size: SIZE,
        sort: SORT,
        keyword: KEYWORD,
    });

    // name 태그에 page를 박아두면 요약/익스포트에서 페이지별로 보기 좋음
    const res = http.get(url, {
        tags: {
            name: `page=${page},size=${SIZE}`,
            page: page,
            brandId: BRAND_ID,
            sort: SORT
        }
    });

    // 기본 체크(필요시 조건 바꿔도 됨)
    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    // 페이지 태그로 레이턴시 기록
    // latencyByPage.add(res.timings.duration, {page: String(page)});

    sleep(SLEEP_SEC);
}

// // ======== 요약(선택) ========
// // --summary-export=summary.json 옵션으로 내보내서 페이지별(latency_by_page{page:..})를 분석하는 걸 추천
// export function handleSummary(data) {
//     // 콘솔에 가볍게 한 줄 메시지
//     return {
//         stdout:
//             `\nDone. Export with --summary-export=summary.json to see per-page trends (metric: latency_by_page, tag: page).\n`,
//     };
// }
