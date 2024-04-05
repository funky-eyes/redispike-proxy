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
package org.redis2asp;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redis2asp.factory.AeroSpikeClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class ServerTest {
    static Server           server;
    static IAerospikeClient aspClient;

    static Logger logger = LoggerFactory.getLogger(ServerTest.class);

    @BeforeAll
    public static void init() throws IOException, ParseException {
        server = new Server();
        server.start("-p6789");
        aspClient = AeroSpikeClientFactory.getClient();
    }

    @Test
    public void testRedisSet() {
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            String result = jedis.set("a", "1");
            try (Jedis jedis2 = new Jedis("127.0.0.1", 6789)) {
                String result2 = jedis2.set("a", "1");
                Assertions.assertEquals(result, result2);
            }
        }
    }

    @Test
    public void testSetAsp() {
        try (Jedis jedis = new Jedis("127.0.0.1", 6789)) {
            String result = jedis.set("a", "1");
            Assertions.assertEquals(result, "OK");
        }
        Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, "a");
        Record record = aspClient.get(aspClient.getReadPolicyDefault(), key);
        Map<String, Object> map = record.bins;
        logger.info("map: {}", map);
        Assertions.assertTrue(map.containsKey("a"));
    }

    @AfterAll
    public static void shutdown() {
        Optional.ofNullable(server).ifPresent(Server::shutdown);
        Optional.ofNullable(aspClient).ifPresent(IAerospikeClient::close);
    }
}
