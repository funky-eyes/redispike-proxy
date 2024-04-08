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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.Value;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.listener.RecordSequenceListener;
import com.aerospike.client.policy.ScanPolicy;
import com.alipay.remoting.RemotingContext;
import com.alipay.sofa.common.profile.StringUtil;

import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.GetRequest;
import icu.funkye.redispike.protocol.request.KeysRequest;
import icu.funkye.redispike.util.IdWorker;
import icu.funkye.redispike.util.IntegerUtils;
import icu.funkye.redispike.util.UUIDGenerator;

public class KeysRequestProcessor extends AbstractRedisRequestProcessor<KeysRequest> {

    final long MAX_ID = (long) Math.pow(10, 14);

    public KeysRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(KeysRequest.class.hashCode()));
    }

    @Override
    public void handle(RemotingContext ctx, KeysRequest request) {
        ScanPolicy scanPolicy = new ScanPolicy(client.getScanPolicyDefault());
        scanPolicy.includeBinData = false;
        scanPolicy.failOnClusterChange = true;
        client.scanAll(AeroSpikeClientFactory.eventLoops.next(), new RecordSequenceListener() {
            @Override
            public void onRecord(Key key, Record record) throws AerospikeException {
                request.setResponse(key.userKey.toString());
            }

            @Override
            public void onSuccess() {
                ctx.writeAndFlush(request.getResponse());
            }

            @Override
            public void onFailure(AerospikeException exception) {
                logger.error(exception.getMessage(), exception);
                ctx.writeAndFlush(request.getResponse());
            }
        }, scanPolicy, AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set);
    }

    /*
        public static void main(String[] args) {
            List<String> list = new ArrayList<>();
            list.add("keys");
            list.add("*");
            KeysRequest keysRequest = new KeysRequest(list);
            Set<String> set = new HashSet<>();
            set.add("1");
            set.add("2");
            keysRequest.setResponse(Arrays.toString(set.toArray(new String[0])).getBytes(StandardCharsets.UTF_8));
            System.out.println(new String(keysRequest.getResponse().data()));

        }
    */

}
