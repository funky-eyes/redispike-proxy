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

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.util.Pool;

/**
 */
public class JedisPooledFactory {
    /**
     * The constant LOGGER.
     */
    protected static final Logger       LOGGER    = LoggerFactory.getLogger(JedisPooledFactory.class);

    private static volatile Pool<Jedis> jedisPool = null;

    /**
     * get the RedisPool instance (singleton)
     *
     * @return redisPool
     */
    public static Pool<Jedis> getJedisPoolInstance(String ip, int port) {
        if (jedisPool == null) {
            synchronized (JedisPooledFactory.class) {
                if (jedisPool == null) {
                    GenericObjectPoolConfig<Jedis> config = new GenericObjectPoolConfig<Jedis>();
                    config.setMaxTotal(100);
                    config.setMinIdle(10);
                    config.setMaxIdle(100);
                    jedisPool = new JedisPool(config, ip, port, 10000);
                }
            }
        }
        return jedisPool;
    }

    /**
     * get an instance of Jedis (connection) from the connection pool
     *
     * @return jedis
     */
    public static Jedis getJedisInstance() {
        return jedisPool.getResource();
    }

}
