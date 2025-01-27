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

import java.util.Optional;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.listener.RecordSequenceListener;
import com.aerospike.client.policy.ScanPolicy;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.util.StringUtils;

import icu.funkye.redispike.conts.RedisConstants;
import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.KeysRequest;
import icu.funkye.redispike.util.IntegerUtils;

public class KeysRequestProcessor extends AbstractRedisRequestProcessor<KeysRequest> {
    ScanPolicy scanPolicy = new ScanPolicy(client.getScanPolicyDefault());

    public KeysRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(KeysRequest.class.hashCode()));
        this.scanPolicy.includeBinData = false;
    }

    @Override
    public void handle(RemotingContext ctx, KeysRequest request) {
        if (StringUtils.isBlank(request.getPattern())) {
            write(ctx, request);
            return;
        }
        boolean all = StringUtils.equals(request.getPattern(), "*");
        boolean left = request.getPattern().startsWith("*");
        if (left) {
            request.setPattern(request.getPattern().substring(1));
        }
        boolean right = request.getPattern().endsWith("*");
        if (right) {
            request.setPattern(request.getPattern().substring(0, request.getPattern().length() - 1));
        }
        client.scanAll(AeroSpikeClientFactory.eventLoops.next(), new RecordSequenceListener() {
            @Override
            public void onRecord(Key key, Record record) throws AerospikeException {
                if (key != null) {
                    if (key.userKey != null) {
                        String userKey = key.userKey.toString();
                        if (all) {
                            request.setResponse(userKey);
                        } else if (left) {
                            if (right) {
                                if (userKey.contains(request.getPattern())) {
                                    request.setResponse(userKey);
                                }
                            } else if (userKey.endsWith(request.getPattern())) {
                                request.setResponse(userKey);
                            }
                        } else if (right) {
                            if (userKey.startsWith(request.getPattern())) {
                                request.setResponse(userKey);
                            }
                        } else {
                            if (StringUtils.equals(userKey, request.getPattern())) {
                                request.setResponse(userKey);
                            }
                        }
                    }
                }
            }

            @Override
            public void onSuccess() {
                write(ctx, request);
            }

            @Override
            public void onFailure(AerospikeException exception) {
                logger.error(exception.getMessage(), exception);
                write(ctx, request);
            }
        }, scanPolicy, AeroSpikeClientFactory.namespace, Optional.ofNullable(ctx.getConnection().getAttribute(
                RedisConstants.REDIS_DB))
                .orElseGet(() -> AeroSpikeClientFactory.set).toString());
    }
}
