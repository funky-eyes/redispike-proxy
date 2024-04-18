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
import com.aerospike.client.Language;
import com.aerospike.client.Record;
import com.aerospike.client.listener.DeleteListener;
import com.aerospike.client.listener.ExecuteListener;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.listener.WriteListener;
import com.aerospike.client.task.RegisterTask;
import com.alipay.remoting.RemotingContext;

import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.hash.HDelRequest;
import icu.funkye.redispike.protocol.request.hash.HLenRequest;
import icu.funkye.redispike.util.IntegerUtils;

public class HLenRequestProcessor extends AbstractRedisRequestProcessor<HLenRequest> {

    public HLenRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(HLenRequest.class.hashCode()));
        RegisterTask task = client.register(null, this.getClass().getClassLoader(), "lua/hlen.lua", "hlen.lua",
            Language.LUA);
        task.waitTillComplete();
    }

    @Override
    public void handle(RemotingContext ctx, HLenRequest request) {
        Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, request.getKey());
        client.execute(AeroSpikeClientFactory.eventLoops.next(), new ExecuteListener() {
            @Override
            public void onSuccess(Key key, Object obj) {
                request.setResponse(obj.toString());
                write(ctx, request);
                logger.info("hlen response:{}", obj);
            }

            @Override
            public void onFailure(AerospikeException exception) {
                logger.error(exception.getMessage(), exception);
                write(ctx, request);
            }
        }, client.getWritePolicyDefault(), key, "hlen", "hash_count_bins");
    }
}
