/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package icu.funkye.redispike;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.SetParams;

public class ServerTest {
    static Server           server;
    static IAerospikeClient aspClient;

    static Logger           logger      = LoggerFactory.getLogger(ServerTest.class);

    private final int       RandomValue = 10000000;

    @BeforeAll
    public static void init() throws ParseException {
        server = new Server();
        server.start("-th 10.58.10.103 -tp 3000 -n test -s tdkv-test -TU tongdun-admin1 -TP xxxzzz123 -p 6789".split(" "));
        JedisPooledFactory.getJedisPoolInstance("127.0.0.1", 6789);
        aspClient = AeroSpikeClientFactory.getClient();
    }

    @Test
    public void TestPippline() {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            keys.add(String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue)));
        }
        String key = String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue));
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            try (Pipeline pipeline = jedis.pipelined()) {
                for (String value : keys) {
                    pipeline.hset(key, value, "b");
                }
                pipeline.syncAndReturnAll();
            }
            jedis.del(key);
            try (Pipeline pipeline = jedis.pipelined()) {
                for (String value : keys) {
                    pipeline.set(value, value);
                }
                pipeline.sync();
                for (String value : keys) {
                    pipeline.get(value);
                }
                List<Object> list = pipeline.syncAndReturnAll();
                for (Object object : list) {
                    Assertions.assertTrue(keys.contains(object.toString()));
                }
            }
            jedis.del(key);
        }
    }

    @Test
    public void TestSet() {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            keys.add(String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue)));
        }
        String key = String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue));
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            for (String value : keys) {
                jedis.sadd(key, value);
            }
            Set<String> list = jedis.smembers(key);
            Assertions.assertNotEquals(list.size(), 3);
            String value = jedis.srandmember(key);
            Assertions.assertTrue(keys.contains(value));
            List<String> values = jedis.srandmember(key, 2);
            Assertions.assertTrue(keys.containsAll(values));
            value = jedis.spop(key);
            Assertions.assertNotNull(value);
            list = jedis.spop(key, 2);
            Assertions.assertNotEquals(list.size(), 2);
            list = jedis.smembers(key);
            Assertions.assertEquals(list.size(), 0);
            for (String value2 : keys) {
                jedis.sadd(key, value2);
            }
            long result = jedis.scard(key);
            Assertions.assertEquals(result, 3);
            result = jedis.srem(key, keys.remove(0));
            Assertions.assertEquals(result, 1);
            result = jedis.srem(key, keys.toArray(new String[0]));
            Assertions.assertEquals(result, 2);
        }
    }

    @Test
    @DisabledIfSystemProperty(named = "asp-client.version", matches = "4.1.2")
    public void testKeys() {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            keys.add(String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue)));
        }
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            for (String key : keys) {
                jedis.set(key, "b");
            }
            Set<String> result = jedis.keys("123");
            Assertions.assertEquals(result.size(), 0);
            result = jedis.keys("*");
            Assertions.assertNotEquals(result.size(), 0);
            jedis.set("abc123", "123");
            result = jedis.keys("abc*");
            Assertions.assertEquals(result.size(), 1);
            result = jedis.keys("*123");
            Assertions.assertEquals(result.size(), 1);
        }
    }

    @Test
    public void testhHash() {
        String key = String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue));
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            Long result = jedis.hset(key.getBytes(StandardCharsets.UTF_8), "b".getBytes(StandardCharsets.UTF_8),
                "c".getBytes(StandardCharsets.UTF_8));
            Assertions.assertEquals(result, 1);
            result = jedis.hdel(key, "b");
            Assertions.assertEquals(result, 1);
            Map<String, String> map = new HashMap<>();
            map.put("b", "c");
            map.put("d", "e");
            result = jedis.hset(key, map);
            Assertions.assertEquals(result, 2);
            List<String> list = jedis.hmget(key, "b", "d");
            Assertions.assertEquals(list.size(), 2);
            list = jedis.hvals(key);
            Assertions.assertEquals(list.size(), 2);
            result = jedis.hdel(key, map.keySet().toArray(new String[0]));
            Assertions.assertEquals(result, 2);
            key = String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue));
            result = jedis.hsetnx(key, "f", "g");
            Assertions.assertEquals(result, 1);
            result = jedis.hsetnx(key, "f", "g");
            Assertions.assertEquals(result, 0);
            String value = jedis.hget(key, "f");
            Assertions.assertEquals(value, "g");
            value = jedis.hget(key, "l");
            Assertions.assertNull(value);
            list = jedis.hmget(key, "l", "p");
            Assertions.assertTrue(list.isEmpty());
            boolean hexists = jedis.hexists(key, "i");
            Assertions.assertFalse(hexists);
            hexists = jedis.hexists(key, "f");
            Assertions.assertTrue(hexists);
            map = jedis.hgetAll(key);
            Assertions.assertEquals(map.size(), 1);
            result = jedis.del(key);
            Assertions.assertEquals(result, 1);
            list = jedis.hvals(key);
            Assertions.assertEquals(list.size(), 0);
        }
    }

    @Test
    public void testRedisSet() {
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            String result = jedis.set("a", "bq");
            try (Jedis jedis2 = new Jedis("127.0.0.1", 6379)) {
                String result2 = jedis2.set("a", "bq");
                Assertions.assertEquals(result, result2);
            }
        }
    }

    @Test
    public void testGetSetAsp() {
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            String result = jedis.set("a", "b");
            Assertions.assertEquals(result, "OK");
        }
        Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, "a");
        Record record = aspClient.get(aspClient.getReadPolicyDefault(), key);
        Map<String, Object> map = record.bins;
        Assertions.assertEquals(map.get(" "), "b");
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            String result = jedis.get("a");
            Assertions.assertEquals(result, "b");
        }
    }

    @Test
    public void testGetNilAsp() {
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            String result = jedis.get(String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue)));
            Assertions.assertNull(result);
        }
    }

    @Test
    public void testSetExAsp() {
        String key = String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue));
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            String result = jedis.set(key, "b", SetParams.setParams().ex(1L));
            Assertions.assertEquals(result, "OK");
            Thread.sleep(3000);
            result = jedis.get(key);
            Assertions.assertNull(result);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSetNxNilAsp() {
        String key = String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue));
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            String result = jedis.set(key, "b", SetParams.setParams().nx());
            Assertions.assertEquals(result, "OK");
            result = jedis.set(key, "b", SetParams.setParams().nx());
            Assertions.assertNull(result);
            key = String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue));
            result = String.valueOf(jedis.setnx(key, "b"));
            Assertions.assertEquals(result, "1");
            result = String.valueOf(jedis.setnx(key, "b"));
            Assertions.assertEquals(result, "0");
        }
    }

    @Test
    public void testSetExNxAsp() {
        String key = String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue));
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            String result = jedis.set(key, "b", SetParams.setParams().nx().ex(1L));
            Assertions.assertEquals(result, "OK");
            Thread.sleep(3000);
            result = jedis.get(key);
            Assertions.assertNull(result);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDelAsp() {
        String key = String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue));
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            String result = jedis.set(key, "b");
            Assertions.assertEquals(result, "OK");
            result = String.valueOf(jedis.del(key));
            Assertions.assertEquals(result, "1");
        }
    }

    @Test
    public void testBatchDelAsp() {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            keys.add(String.valueOf(ThreadLocalRandom.current().nextInt(RandomValue)));
        }
        try (Jedis jedis = JedisPooledFactory.getJedisInstance()) {
            String result = jedis.set(keys.get(0), "b");
            Assertions.assertEquals(result, "OK");
            result = jedis.set(keys.get(1), "b");
            Assertions.assertEquals(result, "OK");
            result = String.valueOf(jedis.del(keys.toArray(new String[0])));
            Assertions.assertEquals(result, String.valueOf(keys.size()));
        }
    }

    @AfterAll
    public static void shutdown() {
        Optional.ofNullable(server).ifPresent(Server::shutdown);
        Optional.ofNullable(aspClient).ifPresent(IAerospikeClient::close);
    }
}
