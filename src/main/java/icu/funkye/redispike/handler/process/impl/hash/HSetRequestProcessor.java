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
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Language;
import com.aerospike.client.listener.ExecuteListener;
import com.aerospike.client.listener.WriteListener;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.task.RegisterTask;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.util.StringUtils;
import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.hash.HSetRequest;
import icu.funkye.redispike.protocol.request.conts.Operate;
import icu.funkye.redispike.util.IntegerUtils;

public class HSetRequestProcessor extends AbstractRedisRequestProcessor<HSetRequest> {
    WritePolicy defaultWritePolicy;

    public HSetRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(HSetRequest.class.hashCode()));
        this.defaultWritePolicy = client.getWritePolicyDefault();
        this.defaultWritePolicy.sendKey = true;
        RegisterTask task = client.register(null, this.getClass().getClassLoader(), "lua/hsetnx.lua", "hsetnx.lua",
            Language.LUA);
        task.waitTillComplete();
    }

    @Override
    public void handle(RemotingContext ctx, HSetRequest request) {
        Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, request.getKey());
        List<Bin> list = new ArrayList<>();
        request.getKv().forEach((k, v) -> {
            Object value;
            if (StringUtils.isNumeric(v)) {
                value = Long.parseLong(v);
            } else if (v.matches("-?\\d+(\\.\\d+)?")) {
                value = Double.parseDouble(v);
            } else {
                value = v;
            }
            list.add(new Bin(k, value));
        });
        WritePolicy writePolicy = defaultWritePolicy;
        if (request.getOperate() != null && request.getOperate() == Operate.NX) {
           Bin bin = list.get(0);
            client.execute(AeroSpikeClientFactory.eventLoops.next(), new ExecuteListener() {
                @Override public void onSuccess(Key key, Object obj) {
                    request.setResponse(obj.toString());
                    write(ctx,request);
                }

                @Override public void onFailure(AerospikeException ae) {
                    logger.error(ae.getMessage(), ae);
                    request.setErrorResponse(ae.getMessage());
                    write(ctx,request);
                }
            }, writePolicy, key, "write_bin_if_not_exists", bin.name, bin.value);
        }else {
            client.put(AeroSpikeClientFactory.eventLoops.next(), new WriteListener() {
                @Override public void onSuccess(Key key) {
                    request.setResponse(String.valueOf(request.getKv().size()));
                    write(ctx, request);
                }

                @Override public void onFailure(AerospikeException ae) {
                    logger.error(ae.getMessage(), ae);
                    write(ctx, request);
                }
            }, writePolicy, key, list.toArray(new Bin[0]));
        }
    }
}
