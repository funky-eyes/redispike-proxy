package org.redis2asp;

import org.junit.Test;
import redis.clients.jedis.Jedis;

public class TestMain {

    @Test
    public void testSet() {
        Jedis jedis = new Jedis("localhost", 8999);
        System.out.println(jedis.set("a", "b"));
        jedis.close();
    }

}
