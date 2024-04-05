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
package org.redis2asp.protocol;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.command.ParticleType;
import com.alipay.remoting.CommandCode;
import com.alipay.remoting.CommandHandler;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.RemotingProcessor;
import org.redis2asp.factory.AeroSpikeClientFactory;
import org.redis2asp.protocol.request.CommandRequest;
import org.redis2asp.protocol.request.SetRequest;

public class RedisCommandHandler implements CommandHandler {
    IAerospikeClient client = AeroSpikeClientFactory.getClient();

    @Override
    public void handleCommand(RemotingContext ctx, Object msg) {
        if (msg instanceof RedisRequest) {
            RedisRequest<?> redisRequest = (RedisRequest) msg;
            if (redisRequest instanceof SetRequest) {
                SetRequest setRequest = (SetRequest)redisRequest;
                Bin bin = new Bin(setRequest.getKey(), setRequest.getValue());
                Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, setRequest.getKey());
                client.add(client.getWritePolicyDefault(), key, bin);
                setRequest.setResponse("OK".getBytes(StandardCharsets.UTF_8));
            }
            if (redisRequest instanceof CommandRequest) {
                CommandRequest commandRequest = (CommandRequest) redisRequest;
                commandRequest.setResponse("OK".getBytes(StandardCharsets.UTF_8));
            }
            ctx.writeAndFlush(redisRequest.getResponse());
        }
    }

    @Override
    public void registerProcessor(CommandCode cmd, RemotingProcessor<?> processor) {

    }

    @Override
    public void registerDefaultExecutor(ExecutorService executor) {

    }

    @Override
    public ExecutorService getDefaultExecutor() {
        return null;
    }
}
