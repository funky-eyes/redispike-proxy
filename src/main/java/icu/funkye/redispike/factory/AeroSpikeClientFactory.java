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
package icu.funkye.redispike.factory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.async.EventLoops;
import com.aerospike.client.async.EventPolicy;
import com.aerospike.client.async.NettyEventLoops;
import com.aerospike.client.async.NioEventLoops;
import com.aerospike.client.policy.ClientPolicy;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;

public class AeroSpikeClientFactory {

    private static final Lock               LOCK = new ReentrantLock();

    private static volatile AerospikeClient client;

    public static volatile String           namespace;

    public static volatile String           set;
    public static volatile EventLoops       eventLoops;
    static {
        EventPolicy eventPolicy = new EventPolicy();
        if (Epoll.isAvailable()) {
            EventLoopGroup group = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors());
            eventLoops = new NettyEventLoops(eventPolicy, group);
        } else {
            eventLoops = new NioEventLoops(eventPolicy, Runtime.getRuntime().availableProcessors());
        }
    }

    /**
     * Return either a native Aerospike client or a proxy client, based on isProxy.
     *
     * @param clientPolicy			client configuration parameters, pass in null for defaults
     * @param hosts					array of server hosts that the client can connect
     */
    public static void createInstance(ClientPolicy clientPolicy, Host... hosts) {
        if (client == null) {
            LOCK.lock();
            try {
                if (client == null) {
                    clientPolicy.eventLoops = eventLoops;
                    client = new AerospikeClient(clientPolicy, hosts);
                }
            } finally {
                LOCK.unlock();
            }
        }
    }

    public static IAerospikeClient getClient() {
        return client;
    }

}
