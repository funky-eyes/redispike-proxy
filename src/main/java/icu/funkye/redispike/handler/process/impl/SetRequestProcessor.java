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
package icu.funkye.redispike.handler.process.impl;

import java.nio.charset.StandardCharsets;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.listener.WriteListener;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.alipay.remoting.RemotingContext;
import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.SetRequest;
import icu.funkye.redispike.protocol.request.conts.Operate;
import icu.funkye.redispike.protocol.request.conts.TtlType;
import icu.funkye.redispike.util.IntegerUtils;

public class SetRequestProcessor extends AbstractRedisRequestProcessor<SetRequest> {
    WritePolicy writePolicy;

    public SetRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(SetRequest.class.hashCode()));
        this.writePolicy = new WritePolicy(client.getWritePolicyDefault());
        this.writePolicy.sendKey = true;
    }

    @Override
    public void handle(RemotingContext ctx, SetRequest request) {
        Bin bin = new Bin(request.getKey(), request.getValue());
        Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, request.getKey());
        WritePolicy writePolicy = this.writePolicy;
        if (request.getTtl() != null) {
            writePolicy = new WritePolicy(writePolicy);
            if (request.getTtlType() == TtlType.EX) {
                writePolicy.expiration = request.getTtl().intValue();
            } else {
                writePolicy.expiration = Integer.max((int) (request.getTtl() / 1000), 1);
            }
        }
        if (request.getOperate() != null) {
            if (request.getOperate() == Operate.NX) {
                writePolicy = new WritePolicy(writePolicy);
                writePolicy.recordExistsAction = RecordExistsAction.CREATE_ONLY;
            } else if (request.getOperate() == Operate.XX) {
                client.get(AeroSpikeClientFactory.eventLoops.next(), new RecordListener() {
                    @Override
                    public void onSuccess(Key key, Record record) {
                        if (record == null) {
                            ctx.writeAndFlush(request.getResponse());
                        } else {
                            client.put(AeroSpikeClientFactory.eventLoops.next(), new WriteListener() {
                                @Override
                                public void onSuccess(Key key) {
                                    request.setResponse("OK");
                                    ctx.writeAndFlush(request.getResponse());
                                }

                                @Override
                                public void onFailure(AerospikeException ae) {
                                    logger.error(ae.getMessage(), ae);
                                    ctx.writeAndFlush(request.getResponse());
                                }
                            }, client.getWritePolicyDefault(), key, bin);
                        }
                    }

                    @Override
                    public void onFailure(AerospikeException ae) {
                        logger.error(ae.getMessage(), ae);
                        ctx.writeAndFlush(request.getResponse());
                    }
                }, client.getReadPolicyDefault(), key);
                return;
            }
        }
        client.put(AeroSpikeClientFactory.eventLoops.next(), new WriteListener() {
            @Override
            public void onSuccess(Key key) {
                if (request.getOriginalCommand().contains("nx")) {
                    request.setResponse("1");
                } else {
                    request.setResponse("OK");
                }
                ctx.writeAndFlush(request.getResponse());
            }

            @Override
            public void onFailure(AerospikeException ae) {
                logger.error(ae.getMessage(), ae);
                ctx.writeAndFlush(request.getResponse());
            }
        }, writePolicy, key, bin);
    }

}
