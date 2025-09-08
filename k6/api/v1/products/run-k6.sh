#!/bin/sh

#export K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write
#export K6_PROMETHEUS_RW_TREND_STATS="avg,min,med,max,p(90),p(95),p(99),p(99.9)"

#k6 run --out experimental-prometheus-rw search-products.js
#k6 run --out web-dashboard search-products.js

# 브랜드, 범위, 정렬, 반복 횟수 조정
#BASE_URL="http://localhost:8080" \
#BRAND_ID=17 \
#PAGES=200 \
#START_PAGE=0 \
#SIZE=20 \
#SORT=POPULAR \
#REPEAT=5 \
#SLEEP_SEC=0.05 \
k6 run \
  ./search-products.js \
  --summary-trend-stats="avg,min,med,max,p(90),p(95),p(99),p(99.9)"
#  --summary-export=summary.json \
