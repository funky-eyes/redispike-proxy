# redispike-proxy
High-performance Aerospike proxy for the Redis protocold

### compatibility

Aerospike: 3.x - 7.x (8.x version has not been tested yet as there is no Docker image available currently)

Redis 3.x - latest

### support mode

Details can be found here: [redispike-proxy/src/test/java/icu/funkye/redispike/ServerTest.java at main · funky-eyes/redispike-proxy (github.com)](https://github.com/funky-eyes/redispike-proxy/blob/main/src/test/java/icu/funkye/redispike/ServerTest.java)

| feature  | support                                                                                                      | note                                                     |
|----------|--------------------------------------------------------------------------------------------------------------|----------------------------------------------------------|
| String   | done                                                                                                         |                                                          |
| Hash     | done                                                                                                         | hsetnx only supports the key level, not the column level |
| Scan     |                                                                                                              |                                                          |
| List     |                                                                                                              |                                                          |
| Set      | scard done<br/>srem done <br/>sadd done<br/>spop done<br/>smembers done <br/>srandmember done<br/>other wait |                                                          |
| ZSet     | wait                                                                                                         |                                                          |
| keys     | done                                                                                                         |                                                          |
| pipeline | done                                                                                                         |                                                          |

### Performance Test Report
aerospike 3.x 2c4g  redispike-proxy 2c4g:

`./redis-benchmark -h xxxx -p 6379 -n 2000000 -c 180 -t get`
```
====== GET ======
  2000000 requests completed in 41.07 seconds
  180 parallel clients
  3 bytes payload
  keep alive: 1
  multi-thread: no

Latency by percentile distribution:
0.000% <= 1.167 milliseconds (cumulative count 1)
50.000% <= 3.215 milliseconds (cumulative count 1002779)
75.000% <= 4.287 milliseconds (cumulative count 1500341)
87.500% <= 5.327 milliseconds (cumulative count 1750471)
93.750% <= 6.487 milliseconds (cumulative count 1875060)
96.875% <= 7.727 milliseconds (cumulative count 1937519)
98.438% <= 9.039 milliseconds (cumulative count 1968840)
99.219% <= 10.335 milliseconds (cumulative count 1984379)
99.609% <= 11.959 milliseconds (cumulative count 1992219)
99.805% <= 13.559 milliseconds (cumulative count 1996101)
99.902% <= 15.215 milliseconds (cumulative count 1998049)
99.951% <= 16.943 milliseconds (cumulative count 1999028)
99.976% <= 19.775 milliseconds (cumulative count 1999512)
99.988% <= 22.575 milliseconds (cumulative count 1999756)
99.994% <= 40.511 milliseconds (cumulative count 1999878)
99.997% <= 42.431 milliseconds (cumulative count 1999939)
99.998% <= 49.663 milliseconds (cumulative count 1999971)
99.999% <= 50.047 milliseconds (cumulative count 1999985)
100.000% <= 50.239 milliseconds (cumulative count 1999993)
100.000% <= 50.367 milliseconds (cumulative count 1999997)
100.000% <= 50.495 milliseconds (cumulative count 1999999)
100.000% <= 52.415 milliseconds (cumulative count 2000000)
100.000% <= 52.415 milliseconds (cumulative count 2000000)

Cumulative distribution of latencies:
0.000% <= 0.103 milliseconds (cumulative count 0)
0.002% <= 1.207 milliseconds (cumulative count 35)
0.063% <= 1.303 milliseconds (cumulative count 1252)
0.312% <= 1.407 milliseconds (cumulative count 6240)
0.802% <= 1.503 milliseconds (cumulative count 16041)
1.724% <= 1.607 milliseconds (cumulative count 34486)
2.999% <= 1.703 milliseconds (cumulative count 59979)
4.846% <= 1.807 milliseconds (cumulative count 96912)
6.978% <= 1.903 milliseconds (cumulative count 139552)
9.776% <= 2.007 milliseconds (cumulative count 195523)
12.656% <= 2.103 milliseconds (cumulative count 253115)
46.802% <= 3.103 milliseconds (cumulative count 936044)
71.720% <= 4.103 milliseconds (cumulative count 1434409)
85.607% <= 5.103 milliseconds (cumulative count 1712133)
92.227% <= 6.103 milliseconds (cumulative count 1844535)
95.630% <= 7.103 milliseconds (cumulative count 1912607)
97.444% <= 8.103 milliseconds (cumulative count 1948873)
98.495% <= 9.103 milliseconds (cumulative count 1969899)
99.116% <= 10.103 milliseconds (cumulative count 1982314)
99.456% <= 11.103 milliseconds (cumulative count 1989124)
99.635% <= 12.103 milliseconds (cumulative count 1992693)
99.761% <= 13.103 milliseconds (cumulative count 1995219)
99.846% <= 14.103 milliseconds (cumulative count 1996921)
99.898% <= 15.103 milliseconds (cumulative count 1997959)
99.931% <= 16.103 milliseconds (cumulative count 1998627)
99.954% <= 17.103 milliseconds (cumulative count 1999076)
99.966% <= 18.111 milliseconds (cumulative count 1999324)
99.972% <= 19.103 milliseconds (cumulative count 1999447)
99.977% <= 20.111 milliseconds (cumulative count 1999541)
99.981% <= 21.103 milliseconds (cumulative count 1999617)
99.985% <= 22.111 milliseconds (cumulative count 1999698)
99.989% <= 23.103 milliseconds (cumulative count 1999778)
99.990% <= 24.111 milliseconds (cumulative count 1999791)
99.990% <= 25.103 milliseconds (cumulative count 1999799)
99.991% <= 26.111 milliseconds (cumulative count 1999820)
99.991% <= 30.111 milliseconds (cumulative count 1999827)
99.991% <= 31.103 milliseconds (cumulative count 1999828)
99.992% <= 39.103 milliseconds (cumulative count 1999842)
99.993% <= 40.127 milliseconds (cumulative count 1999856)
99.995% <= 41.119 milliseconds (cumulative count 1999899)
99.996% <= 42.111 milliseconds (cumulative count 1999930)
99.997% <= 43.103 milliseconds (cumulative count 1999944)
99.997% <= 44.127 milliseconds (cumulative count 1999949)
99.998% <= 45.119 milliseconds (cumulative count 1999951)
99.998% <= 48.127 milliseconds (cumulative count 1999953)
99.998% <= 49.119 milliseconds (cumulative count 1999956)
99.999% <= 50.111 milliseconds (cumulative count 1999988)
100.000% <= 51.103 milliseconds (cumulative count 1999999)
100.000% <= 53.119 milliseconds (cumulative count 2000000)

Summary:
  throughput summary: 48698.53 requests per second
  latency summary (msec):
          avg       min       p50       p95       p99       max
        3.628     1.160     3.215     6.871     9.871    52.415
```