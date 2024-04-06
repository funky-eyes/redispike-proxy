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
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.listener.WriteListener;
import com.alipay.remoting.CommandCode;
import com.alipay.remoting.CommandHandler;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.RemotingProcessor;
import com.alipay.sofa.common.profile.StringUtil;
import org.redis2asp.factory.AeroSpikeClientFactory;
import org.redis2asp.protocol.request.CommandRequest;
import org.redis2asp.protocol.request.GetRequest;
import org.redis2asp.protocol.request.SetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisCommandHandler implements CommandHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    IAerospikeClient     client = AeroSpikeClientFactory.getClient();

    @Override
    public void handleCommand(RemotingContext ctx, Object msg) {
        if (msg instanceof RedisRequest) {
            RedisRequest<?> redisRequest = (RedisRequest) msg;
            if (redisRequest instanceof GetRequest) {
                GetRequest getRequest = (GetRequest) redisRequest;
                Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, getRequest.getKey());
                client.get(null, new RecordListener() {
                    @Override
                    public void onSuccess(Key key, Record record) {
                        logger.info("record: {}", record);
                        String value = record.getString(getRequest.getKey());
                        if (StringUtil.isNotBlank(value)) {
                            getRequest.setResponse(value.getBytes(StandardCharsets.UTF_8));
                        } else {
                            getRequest.setResponse("nil".getBytes(StandardCharsets.UTF_8));
                        }
                        ctx.writeAndFlush(redisRequest.getResponse());
                    }

                    @Override
                    public void onFailure(AerospikeException ae) {
                        logger.error(ae.getMessage(), ae);
                        getRequest.setResponse(ae.getMessage().getBytes(StandardCharsets.UTF_8));
                        ctx.writeAndFlush(redisRequest.getResponse());
                    }
                }, client.getReadPolicyDefault(), key);
            }
            if (redisRequest instanceof SetRequest) {
                SetRequest setRequest = (SetRequest) redisRequest;
                Bin bin = new Bin(setRequest.getKey(), setRequest.getValue());
                Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, setRequest.getKey());
                client.put(null, new WriteListener() {
                    @Override
                    public void onSuccess(Key key) {
                        setRequest.setResponse("OK".getBytes(StandardCharsets.UTF_8));
                        ctx.writeAndFlush(redisRequest.getResponse());
                    }

                    @Override
                    public void onFailure(AerospikeException ae) {
                        setRequest.setResponse(ae.getMessage().getBytes(StandardCharsets.UTF_8));
                        ctx.writeAndFlush(redisRequest.getResponse());
                    }
                }, client.getWritePolicyDefault(), key, bin);
            }
            if (redisRequest instanceof CommandRequest) {
                CommandRequest commandRequest = (CommandRequest) redisRequest;
                commandRequest.setResponse("OK".getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(redisRequest.getResponse());
            }
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
