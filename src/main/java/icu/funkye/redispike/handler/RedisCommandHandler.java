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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import com.alipay.remoting.CommandCode;
import com.alipay.remoting.CommandHandler;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.RemotingProcessor;
import icu.funkye.redispike.handler.process.RedisRequestProcessor;
import icu.funkye.redispike.handler.process.impl.AuthRequestProcessor;
import icu.funkye.redispike.handler.process.impl.GetRequestProcessor;
import icu.funkye.redispike.handler.process.impl.NotSupportProcessor;
import icu.funkye.redispike.handler.process.impl.hash.HDelRequestProcessor;
import icu.funkye.redispike.handler.process.impl.hash.HExistsRequestProcessor;
import icu.funkye.redispike.handler.process.impl.hash.HGetAllRequestProcessor;
import icu.funkye.redispike.handler.process.impl.hash.HGetRequestProcessor;
import icu.funkye.redispike.handler.process.impl.hash.HIncrbyRequestProcessor;
import icu.funkye.redispike.handler.process.impl.hash.HIncrbyfloatRequestProcessor;
import icu.funkye.redispike.handler.process.impl.hash.HKeysRequestProcessor;
import icu.funkye.redispike.handler.process.impl.hash.HLenRequestProcessor;
import icu.funkye.redispike.handler.process.impl.hash.HMgetRequestProcessor;
import icu.funkye.redispike.handler.process.impl.hash.HSetRequestProcessor;
import icu.funkye.redispike.handler.process.impl.hash.HValsRequestProcessor;
import icu.funkye.redispike.handler.process.impl.KeysRequestProcessor;
import icu.funkye.redispike.handler.process.impl.set.SCardRequestProcessor;
import icu.funkye.redispike.handler.process.impl.set.SPopRequestProcessor;
import icu.funkye.redispike.handler.process.impl.set.SRandmemberRequestProcessor;
import icu.funkye.redispike.handler.process.impl.set.SRemRequestProcessor;
import icu.funkye.redispike.handler.process.impl.SetRequestProcessor;
import icu.funkye.redispike.handler.process.impl.CommandRequestProcessor;
import icu.funkye.redispike.handler.process.impl.DelRequestProcessor;
import icu.funkye.redispike.handler.process.impl.set.SAddRequestProcessor;
import icu.funkye.redispike.handler.process.impl.set.SMembersRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequest;
import icu.funkye.redispike.protocol.response.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisCommandHandler implements CommandHandler {

    private final Logger                           logger       = LoggerFactory.getLogger(getClass());

    Map<Short, RemotingProcessor<RedisRequest<?>>> processorMap = new HashMap<>();

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
        HMgetRequestProcessor hMgetRequestProcessor = new HMgetRequestProcessor();
        processorMap.put(hMgetRequestProcessor.getCmdCode().value(), hMgetRequestProcessor);
        SMembersRequestProcessor smembersRequestProcessor = new SMembersRequestProcessor();
        processorMap.put(smembersRequestProcessor.getCmdCode().value(), smembersRequestProcessor);
        SAddRequestProcessor sAddRequestProcessor = new SAddRequestProcessor();
        processorMap.put(sAddRequestProcessor.getCmdCode().value(), sAddRequestProcessor);
        SPopRequestProcessor sPopRequestProcessor = new SPopRequestProcessor();
        processorMap.put(sPopRequestProcessor.getCmdCode().value(), sPopRequestProcessor);
        SRandmemberRequestProcessor sRandmemberRequestProcessor = new SRandmemberRequestProcessor();
        processorMap.put(sRandmemberRequestProcessor.getCmdCode().value(), sRandmemberRequestProcessor);
        SRemRequestProcessor sRemRequestProcessor = new SRemRequestProcessor();
        processorMap.put(sRemRequestProcessor.getCmdCode().value(), sRemRequestProcessor);
        SCardRequestProcessor sCardRequestProcessor = new SCardRequestProcessor();
        processorMap.put(sCardRequestProcessor.getCmdCode().value(), sCardRequestProcessor);
        HExistsRequestProcessor hExistsRequestProcessor = new HExistsRequestProcessor();
        processorMap.put(hExistsRequestProcessor.getCmdCode().value(), hExistsRequestProcessor);
        HValsRequestProcessor hValsRequestProcessor = new HValsRequestProcessor();
        processorMap.put(hValsRequestProcessor.getCmdCode().value(), hValsRequestProcessor);
        HIncrbyRequestProcessor hIncrbyRequestProcessor = new HIncrbyRequestProcessor();
        processorMap.put(hIncrbyRequestProcessor.getCmdCode().value(), hIncrbyRequestProcessor);
        HIncrbyfloatRequestProcessor hIncrbyfloatRequestProcessor = new HIncrbyfloatRequestProcessor();
        processorMap.put(hIncrbyfloatRequestProcessor.getCmdCode().value(), hIncrbyfloatRequestProcessor);
        HLenRequestProcessor hLenRequestProcessor = new HLenRequestProcessor();
        processorMap.put(hLenRequestProcessor.getCmdCode().value(), hLenRequestProcessor);
        HKeysRequestProcessor hKeysRequestProcessor = new HKeysRequestProcessor();
        registryProcessor(hKeysRequestProcessor);
        NotSupportProcessor notSupportProcessor = new NotSupportProcessor();
        registryProcessor(notSupportProcessor);
        AuthRequestProcessor authRequestProcessor = new AuthRequestProcessor();
        registryProcessor(authRequestProcessor);
    }

    private void registryProcessor(RedisRequestProcessor processor) {
        processorMap.put(processor.getCmdCode().value(), processor);
    }

    @Override
    public void handleCommand(RemotingContext ctx, Object msg) {
        if (msg instanceof List) {
            List<Object> list = (List<Object>) msg;
            for (Object object : list) {
                processSingleRequest(ctx, object);
            }
        } else {
            processSingleRequest(ctx, msg);
        }
    }

    private void processSingleRequest(RemotingContext ctx, Object object) {
        if (object instanceof RedisRequest) {
            RedisRequest<?> request = (RedisRequest<?>) object;
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
