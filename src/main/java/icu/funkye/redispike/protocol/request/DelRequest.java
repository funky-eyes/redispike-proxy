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
package icu.funkye.redispike.protocol.request;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import icu.funkye.redispike.protocol.AbstractRedisRequest;
import icu.funkye.redispike.protocol.RedisRequest;
import icu.funkye.redispike.protocol.RedisResponse;
import icu.funkye.redispike.protocol.response.IntegerResponse;

public class DelRequest extends AbstractRedisRequest<String> {

    List<String>    key;

    AtomicInteger   count    = new AtomicInteger(0);

    IntegerResponse response = new IntegerResponse();

    public DelRequest(List<String> key, boolean flush) {
        this.flush = flush;
        this.key = key;
    }

    public List<String> getKey() {
        return key;
    }

    @Override
    public void setResponse(String data) {
        this.response.setData(data);
    }

    public AtomicInteger getCount() {
        return count;
    }

    @Override
    public RedisResponse<String> getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "DelRequest{" + "key='" + key + '\'' + ", response=" + response + '}';
    }
}
