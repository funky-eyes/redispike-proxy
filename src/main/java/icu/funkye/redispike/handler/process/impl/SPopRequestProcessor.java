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

import java.util.List;
import java.util.Objects;

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
import icu.funkye.redispike.protocol.request.SPopRequest;
import icu.funkye.redispike.util.IntegerUtils;

public class SPopRequestProcessor extends AbstractRedisRequestProcessor<SPopRequest> {
    String luaScriptPath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("lua/spop.lua"))
                             .getPath();

    public SPopRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(SPopRequest.class.hashCode()));
        RegisterTask task = client.register(null, SPopRequestProcessor.class.getClassLoader(), "lua/spop.lua",
            "spop.lua", Language.LUA);
        task.waitTillComplete();
    }

    @Override
    public void handle(RemotingContext ctx, SPopRequest request) {
        // Call the Lua script
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
                ctx.writeAndFlush(request.getResponse());
            }

            @Override
            public void onFailure(AerospikeException exception) {
                logger.error(exception.getMessage(), exception);
                ctx.writeAndFlush(request.getResponse());
            }
        }, client.getWritePolicyDefault(), key, "spop", "random_delete_bins", request.getCount() == null ? Value.get(1)
            : Value.get(request.getCount()));
    }
}