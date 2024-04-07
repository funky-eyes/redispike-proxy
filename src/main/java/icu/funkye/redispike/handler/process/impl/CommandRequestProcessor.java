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
import com.alipay.remoting.RemotingContext;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.CommandRequest;
import icu.funkye.redispike.util.IntegerUtils;

public class CommandRequestProcessor extends AbstractRedisRequestProcessor<CommandRequest> {

    public CommandRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(CommandRequest.class.hashCode()));
    }

    @Override
    public void handle(RemotingContext ctx, CommandRequest request) {
        request.setResponse("OK".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(request.getResponse());
    }
}