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

import java.util.Optional;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.policy.WritePolicy;
import com.alipay.remoting.RemotingContext;

import icu.funkye.redispike.conts.RedisConstants;
import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.hash.HIncrbyfloatRequest;
import icu.funkye.redispike.util.IntegerUtils;

public class HIncrbyfloatRequestProcessor extends AbstractRedisRequestProcessor<HIncrbyfloatRequest> {
    WritePolicy defaultWritePolicy;

    public HIncrbyfloatRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(HIncrbyfloatRequest.class.hashCode()));
        this.defaultWritePolicy = client.getWritePolicyDefault();
        this.defaultWritePolicy.sendKey = true;
    }

    @Override
    public void handle(RemotingContext ctx, HIncrbyfloatRequest request) {
        Key key = new Key(AeroSpikeClientFactory.namespace, Optional.ofNullable(ctx.getConnection().getAttribute(
                RedisConstants.REDIS_DB))
                .orElseGet(() -> AeroSpikeClientFactory.set).toString(), request.getKey());
        Bin bin = new Bin(request.getField(), Double.parseDouble(request.getValue()));
        client.operate(AeroSpikeClientFactory.eventLoops.next(), new RecordListener() {
            @Override
            public void onSuccess(Key key, Record record) {
                Object value = record.getValue(request.getField());
                if (value != null) {
                    request.setResponse(String.valueOf(value));
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
