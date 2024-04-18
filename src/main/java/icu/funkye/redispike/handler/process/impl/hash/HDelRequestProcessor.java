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
package icu.funkye.redispike.handler.process.impl.hash;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.listener.DeleteListener;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.listener.WriteListener;
import com.alipay.remoting.RemotingContext;
import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.hash.HDelRequest;
import icu.funkye.redispike.util.IntegerUtils;

public class HDelRequestProcessor extends AbstractRedisRequestProcessor<HDelRequest> {

    public HDelRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(HDelRequest.class.hashCode()));
    }

    @Override public void handle(RemotingContext ctx, HDelRequest request) {
            Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, request.getKey());
            client.get(AeroSpikeClientFactory.eventLoops.next(), new RecordListener() {
                @Override
                public void onSuccess(Key key, Record record) {
                    Map<String, Object> map = record.bins;
                    for (String field : request.getFields()) {
                        map.remove(field);
                    }
                    if (map.isEmpty()) {
                        client.delete(AeroSpikeClientFactory.eventLoops.next(), new DeleteListener() {
                            @Override
                            public void onSuccess(Key key, boolean b) {
                                request.setResponse(
                                        String.valueOf(request.getFields().size()));
                                write(ctx,request);
                            }

                            @Override
                            public void onFailure(AerospikeException exception) {
                                logger.error(exception.getMessage(), exception);
                                write(ctx,request);
                            }
                        }, client.getWritePolicyDefault(), key);
                    } else {
                        List<Bin> newBins = new ArrayList<>();
                        map.forEach((k, v) -> newBins.add(new Bin(k, (String)v)));
                        client.put(AeroSpikeClientFactory.eventLoops.next(), new WriteListener() {
                            @Override
                            public void onSuccess(Key key) {
                                request.setResponse(
                                        String.valueOf(request.getFields().size()));
                                write(ctx,request);
                            }

                            @Override
                            public void onFailure(AerospikeException exception) {
                                logger.error(exception.getMessage(), exception);
                                write(ctx,request);
                            }
                        }, client.getWritePolicyDefault(), key, newBins.toArray(new Bin[0]));
                    }
                }

                @Override
                public void onFailure(AerospikeException exception) {
                    logger.error(exception.getMessage(), exception);
                    write(ctx,request);
                }
            }, client.getReadPolicyDefault(), key);
    }
}
