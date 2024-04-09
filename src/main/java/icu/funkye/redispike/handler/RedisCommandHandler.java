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
package icu.funkye.redispike.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import com.alipay.remoting.CommandCode;
import com.alipay.remoting.CommandHandler;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.RemotingProcessor;
import icu.funkye.redispike.handler.process.impl.GetRequestProcessor;
import icu.funkye.redispike.handler.process.impl.HDelRequestProcessor;
import icu.funkye.redispike.handler.process.impl.HGetAllRequestProcessor;
import icu.funkye.redispike.handler.process.impl.HGetRequestProcessor;
import icu.funkye.redispike.handler.process.impl.HSetRequestProcessor;
import icu.funkye.redispike.handler.process.impl.KeysRequestProcessor;
import icu.funkye.redispike.handler.process.impl.SPopRequestProcessor;
import icu.funkye.redispike.handler.process.impl.SetRequestProcessor;
import icu.funkye.redispike.handler.process.impl.CommandRequestProcessor;
import icu.funkye.redispike.handler.process.impl.DelRequestProcessor;
import icu.funkye.redispike.handler.process.impl.SAddRequestProcessor;
import icu.funkye.redispike.handler.process.impl.SMembersRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequest;
import icu.funkye.redispike.protocol.response.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisCommandHandler implements CommandHandler {

    private final Logger                        logger       = LoggerFactory.getLogger(getClass());

    Map<Short, RemotingProcessor<RedisRequest>> processorMap = new HashMap<>();

    public RedisCommandHandler() {
        CommandRequestProcessor commandRequestProcessor = new CommandRequestProcessor();
        processorMap.put(commandRequestProcessor.getCmdCode().value(), commandRequestProcessor);
        DelRequestProcessor delRequestProcessor = new DelRequestProcessor();
        processorMap.put(delRequestProcessor.getCmdCode().value(), delRequestProcessor);
        GetRequestProcessor getRequestProcessor = new GetRequestProcessor();
        processorMap.put(getRequestProcessor.getCmdCode().value(), getRequestProcessor);
        HSetRequestProcessor hSetRequestProcessor = new HSetRequestProcessor();
        processorMap.put(hSetRequestProcessor.getCmdCode().value(), hSetRequestProcessor);
        HDelRequestProcessor hDelRequestProcessor = new HDelRequestProcessor();
        processorMap.put(hDelRequestProcessor.getCmdCode().value(), hDelRequestProcessor);
        SetRequestProcessor setRequestProcessor = new SetRequestProcessor();
        processorMap.put(setRequestProcessor.getCmdCode().value(), setRequestProcessor);
        KeysRequestProcessor keysRequestProcessor = new KeysRequestProcessor();
        processorMap.put(keysRequestProcessor.getCmdCode().value(), keysRequestProcessor);
        HGetAllRequestProcessor hGetAllRequestProcessor = new HGetAllRequestProcessor();
        processorMap.put(hGetAllRequestProcessor.getCmdCode().value(), hGetAllRequestProcessor);
        HGetRequestProcessor hGetRequestProcessor = new HGetRequestProcessor();
        processorMap.put(hGetRequestProcessor.getCmdCode().value(), hGetRequestProcessor);
        SMembersRequestProcessor smembersRequestProcessor = new SMembersRequestProcessor();
        processorMap.put(smembersRequestProcessor.getCmdCode().value(), smembersRequestProcessor);
        SAddRequestProcessor sAddRequestProcessor = new SAddRequestProcessor();
        processorMap.put(sAddRequestProcessor.getCmdCode().value(), sAddRequestProcessor);
        SPopRequestProcessor sPopRequestProcessor = new SPopRequestProcessor();
        processorMap.put(sPopRequestProcessor.getCmdCode().value(), sPopRequestProcessor);
    }

    @Override
    public void handleCommand(RemotingContext ctx, Object msg) {
        if (msg instanceof RedisRequest) {
            RedisRequest request = (RedisRequest) msg;
            try {
                processorMap.get(request.getCmdCode().value()).process(ctx, request, getDefaultExecutor());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                ctx.writeAndFlush(new BulkResponse());
            }
        }
    }

    @Override
    public void registerProcessor(CommandCode cmd, RemotingProcessor processor) {
        processorMap.put(cmd.value(), processor);
    }

    @Override
    public void registerDefaultExecutor(ExecutorService executor) {
    }

    @Override
    public ExecutorService getDefaultExecutor() {
        return ForkJoinPool.commonPool();
    }
}
