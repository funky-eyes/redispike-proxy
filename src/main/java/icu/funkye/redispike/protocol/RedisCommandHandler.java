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
package icu.funkye.redispike.protocol;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.listener.DeleteListener;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.listener.WriteListener;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.alipay.remoting.CommandCode;
import com.alipay.remoting.CommandHandler;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.RemotingProcessor;
import com.alipay.sofa.common.profile.StringUtil;
import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.protocol.request.CommandRequest;
import icu.funkye.redispike.protocol.request.DelRequest;
import icu.funkye.redispike.protocol.request.GetRequest;
import icu.funkye.redispike.protocol.request.HDelRequest;
import icu.funkye.redispike.protocol.request.HSetRequest;
import icu.funkye.redispike.protocol.request.SetRequest;
import icu.funkye.redispike.protocol.request.conts.Operate;
import icu.funkye.redispike.protocol.request.conts.TtlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisCommandHandler implements CommandHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    IAerospikeClient     client = AeroSpikeClientFactory.getClient();

    @Override
    public void handleCommand(RemotingContext ctx, Object msg) {
        if (msg instanceof RedisRequest) {
            RedisRequest<?> redisRequest = (RedisRequest)msg;
            if (logger.isDebugEnabled()) {
                logger.debug("redisRequest:{}", redisRequest);
            }
            if (redisRequest instanceof HDelRequest) {
                HDelRequest request = (HDelRequest)redisRequest;
                Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, request.getKey());
                client.get(AeroSpikeClientFactory.eventLoops.next(), new RecordListener() {
                    @Override
                    public void onSuccess(Key key, Record record) {
                        Map<String, Object> map = record.bins;
                        for (String field : ((HDelRequest)redisRequest).getFields()) {
                            map.remove(field);
                        }
                        if (map.isEmpty()) {
                            client.delete(AeroSpikeClientFactory.eventLoops.next(), new DeleteListener() {
                                @Override
                                public void onSuccess(Key key, boolean b) {
                                    request.setResponse(
                                        String.valueOf(request.getFields().size()).getBytes(StandardCharsets.UTF_8));
                                    ctx.writeAndFlush(redisRequest.getResponse());
                                }

                                @Override
                                public void onFailure(AerospikeException exception) {
                                    logger.error(exception.getMessage(), exception);
                                    ctx.writeAndFlush(redisRequest.getResponse());
                                }
                            }, client.getWritePolicyDefault(), key);
                        } else {
                            List<Bin> newBins = new ArrayList<>();
                            map.forEach((k, v) -> newBins.add(new Bin(k, v)));
                            client.put(AeroSpikeClientFactory.eventLoops.next(), new WriteListener() {
                                @Override
                                public void onSuccess(Key key) {
                                    request.setResponse(
                                        String.valueOf(request.getFields().size()).getBytes(StandardCharsets.UTF_8));
                                    ctx.writeAndFlush(redisRequest.getResponse());
                                }

                                @Override
                                public void onFailure(AerospikeException exception) {
                                    logger.error(exception.getMessage(), exception);
                                    ctx.writeAndFlush(redisRequest.getResponse());
                                }
                            }, client.getWritePolicyDefault(), key, newBins.toArray(new Bin[0]));
                        }
                    }

                    @Override
                    public void onFailure(AerospikeException exception) {
                        logger.error(exception.getMessage(), exception);
                        ctx.writeAndFlush(redisRequest.getResponse());
                    }
                }, client.getReadPolicyDefault(), key);
            }
            if (redisRequest instanceof HSetRequest) {
                HSetRequest request = (HSetRequest)redisRequest;
                Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, request.getKey());
                List<Bin> list = new ArrayList<>();
                request.getKv().forEach((k, v) -> list.add(new Bin(k, v)));
                WritePolicy writePolicy;
                if (request.getOperate() != null && request.getOperate() == Operate.NX) {
                    writePolicy = new WritePolicy(client.getWritePolicyDefault());
                    writePolicy.recordExistsAction = RecordExistsAction.CREATE_ONLY;
                } else {
                    writePolicy = client.getWritePolicyDefault();
                }
                client.put(AeroSpikeClientFactory.eventLoops.next(), new WriteListener() {
                    @Override
                    public void onSuccess(Key key) {
                        request.setResponse(String.valueOf(request.getKv().size()).getBytes(StandardCharsets.UTF_8));
                        ctx.writeAndFlush(redisRequest.getResponse());
                    }

                    @Override
                    public void onFailure(AerospikeException ae) {
                        logger.error(ae.getMessage(), ae);
                        ctx.writeAndFlush(redisRequest.getResponse());
                    }
                }, writePolicy, key, list.toArray(new Bin[0]));
            }
            if (redisRequest instanceof GetRequest) {
                GetRequest getRequest = (GetRequest)redisRequest;
                Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, getRequest.getKey());
                client.get(AeroSpikeClientFactory.eventLoops.next(), new RecordListener() {
                    @Override
                    public void onSuccess(Key key, Record record) {
                        if (record == null) {
                            ctx.writeAndFlush(redisRequest.getResponse());
                            return;
                        }
                        String value = record.getString(getRequest.getKey());
                        if (StringUtil.isNotBlank(value)) {
                            getRequest.setResponse(value.getBytes(StandardCharsets.UTF_8));
                        }
                        ctx.writeAndFlush(redisRequest.getResponse());
                    }

                    @Override
                    public void onFailure(AerospikeException ae) {
                        logger.error(ae.getMessage(), ae);
                        ctx.writeAndFlush(redisRequest.getResponse());
                    }
                }, client.getReadPolicyDefault(), key);
            }
            if (redisRequest instanceof SetRequest) {
                SetRequest setRequest = (SetRequest)redisRequest;
                Bin bin = new Bin(setRequest.getKey(), setRequest.getValue());
                Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, setRequest.getKey());
                WritePolicy writePolicy = null;
                if (setRequest.getTtl() != null) {
                    writePolicy = new WritePolicy(client.getWritePolicyDefault());
                    if (setRequest.getTtlType() == TtlType.EX) {
                        writePolicy.expiration = setRequest.getTtl().intValue();
                    } else {
                        writePolicy.expiration = Integer.max((int)(setRequest.getTtl() / 1000), 1);
                    }
                }
                if (setRequest.getOperate() != null) {
                    if (writePolicy == null) {
                        writePolicy = new WritePolicy(client.getWritePolicyDefault());
                    }
                    if (setRequest.getOperate() == Operate.NX) {
                        writePolicy.recordExistsAction = RecordExistsAction.CREATE_ONLY;
                    } else if (setRequest.getOperate() == Operate.XX) {
                        client.get(AeroSpikeClientFactory.eventLoops.next(), new RecordListener() {
                            @Override
                            public void onSuccess(Key key, Record record) {
                                if (record == null) {
                                    ctx.writeAndFlush(redisRequest.getResponse());
                                } else {
                                    client.put(AeroSpikeClientFactory.eventLoops.next(), new WriteListener() {
                                        @Override
                                        public void onSuccess(Key key) {
                                            setRequest.setResponse("OK".getBytes(StandardCharsets.UTF_8));
                                            ctx.writeAndFlush(redisRequest.getResponse());
                                        }

                                        @Override
                                        public void onFailure(AerospikeException ae) {
                                            logger.error(ae.getMessage(), ae);
                                            ctx.writeAndFlush(redisRequest.getResponse());
                                        }
                                    }, client.getWritePolicyDefault(), key, bin);
                                }
                            }

                            @Override
                            public void onFailure(AerospikeException ae) {
                                logger.error(ae.getMessage(), ae);
                                ctx.writeAndFlush(redisRequest.getResponse());
                            }
                        }, client.getReadPolicyDefault(), key);
                        return;
                    }
                }
                if (writePolicy == null) {
                    writePolicy = client.getWritePolicyDefault();
                }
                client.put(AeroSpikeClientFactory.eventLoops.next(), new WriteListener() {
                    @Override
                    public void onSuccess(Key key) {
                        if (setRequest.getOriginalCommand().contains("nx")) {
                            setRequest.setResponse("1".getBytes(StandardCharsets.UTF_8));
                        } else {
                            setRequest.setResponse("OK".getBytes(StandardCharsets.UTF_8));
                        }
                        ctx.writeAndFlush(redisRequest.getResponse());
                    }

                    @Override
                    public void onFailure(AerospikeException ae) {
                        logger.error(ae.getMessage(), ae);
                        ctx.writeAndFlush(redisRequest.getResponse());
                    }
                }, writePolicy, key, bin);
            }
            if (redisRequest instanceof CommandRequest) {
                CommandRequest commandRequest = (CommandRequest)redisRequest;
                commandRequest.setResponse("OK".getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(redisRequest.getResponse());
            }
            if (redisRequest instanceof DelRequest) {
                DelRequest delRequest = (DelRequest)redisRequest;
                List<String> keys = delRequest.getKey();
                List<Key> list =
                    keys.stream().map(key -> new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, key))
                        .collect(Collectors.toList());
                CountDownLatch countDownLatch = new CountDownLatch(list.size());
                for (Key key : list) {
                    client.delete(AeroSpikeClientFactory.eventLoops.next(), new DeleteListener() {
                        @Override
                        public void onSuccess(Key key, boolean b) {
                            delRequest.setResponse(String.valueOf(delRequest.getCount().incrementAndGet())
                                .getBytes(StandardCharsets.UTF_8));
                            countDownLatch.countDown();
                        }

                        @Override
                        public void onFailure(AerospikeException e) {
                            countDownLatch.countDown();
                        }
                    }, client.getWritePolicyDefault(), key);
                }
                CompletableFuture.runAsync(() -> {
                    try {
                        countDownLatch.await(10, TimeUnit.SECONDS);
                        ctx.writeAndFlush(delRequest.getResponse());
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                        ctx.writeAndFlush(delRequest.getResponse());
                    }
                });
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
