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
package icu.funkye.redispike.protocol;

import com.alipay.remoting.CommandDecoder;
import com.alipay.remoting.CommandEncoder;
import com.alipay.remoting.CommandFactory;
import com.alipay.remoting.CommandHandler;
import com.alipay.remoting.HeartbeatTrigger;
import com.alipay.remoting.Protocol;
import icu.funkye.redispike.handler.RedisCommandHandler;

public class RedisProtocol implements Protocol {

    public static final byte PROTOCOL_CODE       = (byte) 42;

    RedisCommandDecoder      redisCommandDecoder = new RedisCommandDecoder();

    RedisCommandEncoder      redisCommandEncoder = new RedisCommandEncoder();

    RedisCommandHandler      redisCommandHandler = new RedisCommandHandler();

    @Override
    public CommandEncoder getEncoder() {
        return redisCommandEncoder;
    }

    @Override
    public CommandDecoder getDecoder() {
        return redisCommandDecoder;
    }

    @Override
    public HeartbeatTrigger getHeartbeatTrigger() {
        return null;
    }

    @Override
    public CommandHandler getCommandHandler() {
        return redisCommandHandler;
    }

    @Override
    public CommandFactory getCommandFactory() {
        return null;
    }
}
