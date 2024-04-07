# redis2asp
High-performance Aerospike proxy for the Redis protocol
### Performance Test Report
aerospike 3.x 2c4g  redispike-proxy 2c4g:
./redis-benchmark -h xxxx -p 6379 -n 2000000 -c 200 -t get
```
WARNING: Could not fetch server CONFIG
====== GET ======
2000000 requests completed in 44.94 seconds
200 parallel clients
3 bytes payload
keep alive: 1
multi-thread: no

Latency by percentile distribution:
0.000% <= 1.975 milliseconds (cumulative count 1)
50.000% <= 3.879 milliseconds (cumulative count 1001535)
75.000% <= 4.919 milliseconds (cumulative count 1501029)
87.500% <= 5.999 milliseconds (cumulative count 1750484)
93.750% <= 7.247 milliseconds (cumulative count 1875200)
96.875% <= 8.599 milliseconds (cumulative count 1937780)
98.438% <= 10.071 milliseconds (cumulative count 1968821)
99.219% <= 12.023 milliseconds (cumulative count 1984401)
99.609% <= 13.967 milliseconds (cumulative count 1992202)
99.805% <= 16.071 milliseconds (cumulative count 1996100)
99.902% <= 19.199 milliseconds (cumulative count 1998053)
99.951% <= 22.271 milliseconds (cumulative count 1999027)
99.976% <= 24.847 milliseconds (cumulative count 1999515)
99.988% <= 26.831 milliseconds (cumulative count 1999756)
99.994% <= 30.527 milliseconds (cumulative count 1999879)
99.997% <= 31.231 milliseconds (cumulative count 1999942)
99.998% <= 31.535 milliseconds (cumulative count 1999971)
99.999% <= 31.663 milliseconds (cumulative count 1999988)
100.000% <= 31.743 milliseconds (cumulative count 1999993)
100.000% <= 31.807 milliseconds (cumulative count 1999997)
100.000% <= 31.871 milliseconds (cumulative count 1999999)
100.000% <= 31.919 milliseconds (cumulative count 2000000)
100.000% <= 31.919 milliseconds (cumulative count 2000000)

Cumulative distribution of latencies:
0.000% <= 0.103 milliseconds (cumulative count 0)
0.001% <= 2.007 milliseconds (cumulative count 10)
0.051% <= 2.103 milliseconds (cumulative count 1012)
22.646% <= 3.103 milliseconds (cumulative count 452915)
56.697% <= 4.103 milliseconds (cumulative count 1133935)
78.001% <= 5.103 milliseconds (cumulative count 1560018)
88.276% <= 6.103 milliseconds (cumulative count 1765529)
93.275% <= 7.103 milliseconds (cumulative count 1865499)
95.983% <= 8.103 milliseconds (cumulative count 1919661)
97.593% <= 9.103 milliseconds (cumulative count 1951867)
98.460% <= 10.103 milliseconds (cumulative count 1969205)
98.949% <= 11.103 milliseconds (cumulative count 1978987)
99.238% <= 12.103 milliseconds (cumulative count 1984763)
99.451% <= 13.103 milliseconds (cumulative count 1989027)
99.629% <= 14.103 milliseconds (cumulative count 1992578)
99.732% <= 15.103 milliseconds (cumulative count 1994648)
99.808% <= 16.103 milliseconds (cumulative count 1996153)
99.853% <= 17.103 milliseconds (cumulative count 1997051)
99.878% <= 18.111 milliseconds (cumulative count 1997563)
99.899% <= 19.103 milliseconds (cumulative count 1997985)
99.918% <= 20.111 milliseconds (cumulative count 1998363)
99.936% <= 21.103 milliseconds (cumulative count 1998710)
99.949% <= 22.111 milliseconds (cumulative count 1998981)
99.962% <= 23.103 milliseconds (cumulative count 1999237)
99.969% <= 24.111 milliseconds (cumulative count 1999375)
99.979% <= 25.103 milliseconds (cumulative count 1999573)
99.984% <= 26.111 milliseconds (cumulative count 1999674)
99.990% <= 27.103 milliseconds (cumulative count 1999795)
99.991% <= 28.111 milliseconds (cumulative count 1999828)
99.993% <= 29.103 milliseconds (cumulative count 1999854)
99.993% <= 30.111 milliseconds (cumulative count 1999862)
99.997% <= 31.103 milliseconds (cumulative count 1999932)
100.000% <= 32.111 milliseconds (cumulative count 2000000)

Summary:
throughput summary: 44501.80 requests per second
latency summary (msec):
avg       min       p50       p95       p99       max
4.337     1.968     3.879     7.679    11.255    31.919
```