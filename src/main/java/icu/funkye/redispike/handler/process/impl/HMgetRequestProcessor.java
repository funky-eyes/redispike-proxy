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
import com.aerospike.client.listener.RecordListener;
import com.alipay.remoting.RemotingContext;

import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.HMgetRequest;
import icu.funkye.redispike.util.IntegerUtils;

public class HMgetRequestProcessor extends AbstractRedisRequestProcessor<HMgetRequest> {

    public HMgetRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(HMgetRequest.class.hashCode()));
    }

    @Override
    public void handle(RemotingContext ctx, HMgetRequest request) {
        Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, request.getKey());
        client.get(AeroSpikeClientFactory.eventLoops.next(), new RecordListener() {
            @Override
            public void onSuccess(Key key, Record record) {
                if (record == null) {
                    write(ctx,request);
                    return;
                }
                Optional.ofNullable(record.bins)
                    .ifPresent(bins -> bins.values().forEach(v -> request.setResponse(v.toString())));
                write(ctx, request);
            }

            @Override
            public void onFailure(AerospikeException ae) {
                logger.error(ae.getMessage(), ae);
                write(ctx,request);
            }
        }, client.getReadPolicyDefault(), key,request.getField().toArray(new String[0]));
    }
}
