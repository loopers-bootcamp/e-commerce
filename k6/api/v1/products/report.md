# Version 1

```text
         /\      Grafana   /‾‾/
    /\  /  \     |\  __   /  /
   /  \/    \    | |/ /  /   ‾‾\
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/

     execution: local
        script: search-products-v1.js
        output: -

     scenarios: (100.00%) 1 scenario, 1 max VUs, 10m30s max duration (incl. graceful stop):
              * default: 360 iterations shared among 1 VUs (maxDuration: 10m0s, gracefulStop: 30s)



  █ TOTAL RESULTS

    checks_total.......................: 360     5.642773/s
    checks_succeeded...................: 100.00% 360 out of 360
    checks_failed......................: 0.00%   0 out of 360

    ✓ status is 200

    CUSTOM
    latency_by_page.........................................................: avg=76.61ms  min=60.3ms   med=75.2ms   max=114.06ms p(90)=90.63ms p(95)=94.92ms  p(99)=104.22ms p(99.9)=111.8ms

    HTTP
    http_req_duration.......................................................: avg=76.61ms  min=60.3ms   med=75.2ms   max=114.06ms p(90)=90.63ms p(95)=94.92ms  p(99)=104.22ms p(99.9)=111.8ms
      { expected_response:true }............................................: avg=76.61ms  min=60.3ms   med=75.2ms   max=114.06ms p(90)=90.63ms p(95)=94.92ms  p(99)=104.22ms p(99.9)=111.8ms
    http_req_failed.........................................................: 0.00%  0 out of 360
    http_reqs...............................................................: 360    5.642773/s

    EXECUTION
    iteration_duration......................................................: avg=177.21ms min=160.48ms med=175.55ms max=214.18ms p(90)=191.7ms p(95)=195.79ms p(99)=204.65ms p(99.9)=211.93ms
    iterations..............................................................: 360    5.642773/s
    vus.....................................................................: 1      min=1        max=1
    vus_max.................................................................: 1      min=1        max=1

    NETWORK
    data_received...........................................................: 855 kB 13 kB/s
    data_sent...............................................................: 45 kB  700 B/s




running (01m03.8s), 0/1 VUs, 360 complete and 0 interrupted iterations
default ✓ [======================================] 1 VUs  01m03.8s/10m0s  360/360 shared iters
```
