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

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import com.alipay.remoting.RemotingContext;
import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.AuthRequest;
import icu.funkye.redispike.protocol.request.NotSupportRequest;
import icu.funkye.redispike.util.IntegerUtils;

/**
 * @author jianbin@apache.org
 */
public class AuthRequestProcessor extends AbstractRedisRequestProcessor<AuthRequest> {

    public AuthRequestProcessor() {
        this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(AuthRequestProcessor.class.hashCode()));
    }

    @Override
	public void handle(RemotingContext ctx, AuthRequest request) {
        if (AeroSpikeClientFactory.originClientPolicy.password == null
            || Objects.equals(AeroSpikeClientFactory.originClientPolicy.password, request.getCommand())) {
            request.setResponse("OK");
        } else {
            request.setErrorResponse("ERR Client sent AUTH, but no password is set");
        }
		Optional.ofNullable(request.getCountDownLatch()).ifPresent(CountDownLatch::countDown);
		write(ctx, request);
	}
}