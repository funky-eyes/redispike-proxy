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
package icu.funkye.redispike.handler.process;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import com.aerospike.client.IAerospikeClient;
import com.alipay.remoting.CommandCode;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.RemotingContext;
import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.protocol.AbstractRedisRequest;
import icu.funkye.redispike.protocol.RedisRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRedisRequestProcessor<T extends RedisRequest<?>> implements RedisRequestProcessor<T> {

    protected final IAerospikeClient client = AeroSpikeClientFactory.getClient();

    protected final Logger           logger = LoggerFactory.getLogger(getClass());

    protected CommandCode            cmdCode;

    @Override
    public void process(RemotingContext ctx, RemotingCommand msg, ExecutorService defaultExecutor) throws Exception {
        if (defaultExecutor != null) {
            defaultExecutor.submit(() -> {
                try {
                    this.handle(ctx, (T)msg);
                } catch (Exception e) {
                    logger.error("process error: {}",e.getMessage(), e);
                }
            });
        } else {
            this.handle(ctx, (T)msg);
        }
    }

    @Override
    public CommandCode getCmdCode() {
        return this.cmdCode;
    }

    public void write(RemotingContext ctx, AbstractRedisRequest request) {
        CountDownLatch countDownLatch = request.getCountDownLatch();
        if (request.isFlush()) {
            if (countDownLatch != null) {
                try {
                    countDownLatch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("writeAndFlush response:{}", request.getResponse());
            }
            ctx.writeAndFlush(request.getResponse());
        } else {
            ctx.getChannelContext().write(request.getResponse());
            countDownLatch.countDown();
            if (logger.isDebugEnabled()) {
                logger.debug("write response:{}", request.getResponse());
            }
        }
    }

}
