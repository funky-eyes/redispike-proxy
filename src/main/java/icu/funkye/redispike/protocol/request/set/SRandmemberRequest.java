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
package icu.funkye.redispike.protocol.request.set;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import icu.funkye.redispike.protocol.RedisResponse;
import icu.funkye.redispike.protocol.response.BulkResponse;
import icu.funkye.redispike.protocol.AbstractRedisRequest;

public class SRandmemberRequest extends AbstractRedisRequest<String> {

    String       key;

    int          sum;

    BulkResponse response;

    public SRandmemberRequest(String key, int sum, boolean flush) {
        this.flush = flush;
        this.key = key;
        this.sum = sum;
        if (sum > 1) {
            this.response = new BulkResponse(new ArrayList<>());
        } else {
            this.response = new BulkResponse();
        }
    }

    public String getKey() {
        return key;
    }

    @Override
    public void setResponse(String data) {
        this.response.setData(data);
    }

    @Override
    public RedisResponse<String> getResponse() {
        return response;
    }

    public int getSum() {
        return sum;
    }

    @Override
    public String toString() {
        return "SRandmemberRequest{" + "key='" + key + '\'' + ", count=" + sum + ", response=" + response + '}';
    }

}
