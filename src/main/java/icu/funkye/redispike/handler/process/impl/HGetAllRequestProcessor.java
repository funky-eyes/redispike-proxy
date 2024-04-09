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

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.listener.RecordListener;
import com.alipay.remoting.RemotingContext;
import com.alipay.sofa.common.profile.StringUtil;

import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.HGetAllRequest;
import icu.funkye.redispike.protocol.request.HGetRequest;
import icu.funkye.redispike.util.IntegerUtils;

public class HGetAllRequestProcessor extends AbstractRedisRequestProcessor<HGetAllRequest> {

    public HGetAllRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(HGetAllRequest.class.hashCode()));
    }

    @Override
    public void handle(RemotingContext ctx, HGetAllRequest request) {
        Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, request.getKey());
        client.get(AeroSpikeClientFactory.eventLoops.next(), new RecordListener() {
            @Override
            public void onSuccess(Key key, Record record) {
                if (record == null) {
                    ctx.writeAndFlush(request.getResponse());
                    return;
                }
                record.bins.forEach((k,v)-> {
                    request.setResponse(k);
                    request.setResponse(v.toString());
                });
                ctx.writeAndFlush(request.getResponse());
            }

            @Override
            public void onFailure(AerospikeException ae) {
                logger.error(ae.getMessage(), ae);
                ctx.writeAndFlush(request.getResponse());
            }
        }, client.getReadPolicyDefault(), key);
    }
}
