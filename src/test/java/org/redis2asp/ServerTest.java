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

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import com.github.microwww.redis.RedisServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

public class ServerTest {
    static RedisServer redisServer;
    static Server server;

    @BeforeAll
    public static void init() throws IOException {
        File cgroupFile = new File("/proc/1/cgroup");
        if (!cgroupFile.exists()) {
            redisServer = new RedisServer();
            redisServer.listener("127.0.0.1", 6379);
        }
        server = new Server();
        server.start(new String[] {"6789"});
    }

    @Test
    public void testSet() {
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            String result = jedis.set("a", "b");
            try (Jedis jedis2 = new Jedis("127.0.0.1", 6789)) {
                String result2 = jedis2.set("a", "b");
                Assertions.assertEquals(result, result2);
            }
        }
    }

    @AfterAll
    public static void shutdown() {
        Optional.ofNullable(redisServer).ifPresent(redisServer1 -> {
            try {
                redisServer1.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Optional.ofNullable(server).ifPresent(Server::shutdown);
    }

}
