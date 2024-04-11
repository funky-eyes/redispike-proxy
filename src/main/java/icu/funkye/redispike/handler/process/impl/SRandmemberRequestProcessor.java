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
import com.aerospike.client.Language;
import com.aerospike.client.Value;
import com.aerospike.client.listener.ExecuteListener;
import com.aerospike.client.task.RegisterTask;
import com.alipay.remoting.RemotingContext;

import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.SRandmemberRequest;
import icu.funkye.redispike.util.IntegerUtils;

public class SRandmemberRequestProcessor extends AbstractRedisRequestProcessor<SRandmemberRequest> {

    public SRandmemberRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(SRandmemberRequest.class.hashCode()));
        RegisterTask task = client.register(null, this.getClass().getClassLoader(), "lua/srandmember.lua",
            "srandmember.lua", Language.LUA);
        task.waitTillComplete();
    }

    @Override
    public void handle(RemotingContext ctx, SRandmemberRequest request) {
        Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, request.getKey());
        client.execute(AeroSpikeClientFactory.eventLoops.next(), new ExecuteListener() {
            @Override
            public void onSuccess(Key key, Object obj) {
                if (obj instanceof String) {
                    String[] response = ((String) obj).split(",");
                    for (String s : response) {
                        request.setResponse(s);
                    }
                }
                write(ctx, request);
            }

            @Override
            public void onFailure(AerospikeException exception) {
                logger.error(exception.getMessage(), exception);
                write(ctx, request);
            }
        }, client.getWritePolicyDefault(), key, "srandmember", "getBinNames", Value.get(request.getSum()));
    }
}
