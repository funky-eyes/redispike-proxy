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

import java.util.ArrayList;
import java.util.List;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.listener.WriteListener;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.alipay.remoting.RemotingContext;

import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.HIncrbyRequest;
import icu.funkye.redispike.protocol.request.HSetRequest;
import icu.funkye.redispike.protocol.request.conts.Operate;
import icu.funkye.redispike.util.IntegerUtils;

public class HIncrbyRequestProcessor extends AbstractRedisRequestProcessor<HIncrbyRequest> {
    WritePolicy defaultWritePolicy;

    public HIncrbyRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(HIncrbyRequest.class.hashCode()));
        this.defaultWritePolicy = client.getWritePolicyDefault();
        this.defaultWritePolicy.sendKey = true;
    }

    @Override
    public void handle(RemotingContext ctx, HIncrbyRequest request) {
        Key key = new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, request.getKey());
        Object value;
        if (request.getValue().contains(".")) {
            value = Double.parseDouble(request.getValue());
        } else {
            value = Long.parseLong(request.getValue());
        }
        Bin bin = new Bin(request.getField(), value);
        client.operate(AeroSpikeClientFactory.eventLoops.next(), new RecordListener() {
            @Override
            public void onSuccess(Key key, Record record) {
                Object value = record.getValue(request.getField());
                if (value != null) {
                    request.setResponse(value.toString());
                }
                write(ctx, request);
            }

            @Override
            public void onFailure(AerospikeException exception) {
                logger.error(exception.getMessage(), exception);
                write(ctx, request);
            }
        }, defaultWritePolicy, key, Operation.add(bin), Operation.get(request.getField()));
    }

}
