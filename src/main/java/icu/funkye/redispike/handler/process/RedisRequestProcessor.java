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

import java.util.concurrent.ExecutorService;
import com.alipay.remoting.CommandCode;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.RemotingProcessor;
import icu.funkye.redispike.protocol.RedisRequest;

public interface RedisRequestProcessor<T extends RedisRequest<?>> extends RemotingProcessor {

    void handle(RemotingContext ctx, T request);

    CommandCode getCmdCode();

    @Override
    default ExecutorService getExecutor() {
        return null;
    }

    @Override
    default void setExecutor(ExecutorService executor) {}
}
