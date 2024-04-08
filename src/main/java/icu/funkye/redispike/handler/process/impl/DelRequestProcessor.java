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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.listener.DeleteListener;
import com.alipay.remoting.RemotingContext;
import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.DelRequest;
import icu.funkye.redispike.util.IntegerUtils;

public class DelRequestProcessor extends AbstractRedisRequestProcessor<DelRequest> {

    public DelRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(DelRequest.class.hashCode()));
    }

    @Override
    public void handle(RemotingContext ctx, DelRequest request) {
        List<String> keys = request.getKey();
        List<Key> list =
            keys.stream().map(key -> new Key(AeroSpikeClientFactory.namespace, AeroSpikeClientFactory.set, key))
                .collect(Collectors.toList());
        CountDownLatch countDownLatch = new CountDownLatch(list.size());
        for (Key key : list) {
            client.delete(AeroSpikeClientFactory.eventLoops.next(), new DeleteListener() {
                @Override
                public void onSuccess(Key key, boolean b) {
                    request.setResponse(String.valueOf(request.getCount().incrementAndGet()));
                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(AerospikeException e) {
                    countDownLatch.countDown();
                }
            }, client.getWritePolicyDefault(), key);
        }
        CompletableFuture.runAsync(() -> {
            try {
                countDownLatch.await(10, TimeUnit.SECONDS);
                ctx.writeAndFlush(request.getResponse());
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                ctx.writeAndFlush(request.getResponse());
            }
        });
    }
}
