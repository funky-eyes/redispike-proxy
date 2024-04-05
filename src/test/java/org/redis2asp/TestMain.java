package org.redis2asp;

import redis.clients.jedis.Jedis;

public class TestMain {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 8999);
        System.out.println(jedis.set("a", "b"));
        jedis.close();
        jedis = new Jedis("localhost", 6379);
        System.out.println(jedis.set("a", "b"));
        jedis.close();
    }

}
