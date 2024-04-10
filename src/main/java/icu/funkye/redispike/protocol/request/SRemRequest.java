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

import java.util.ArrayList;
import java.util.List;

import com.alipay.remoting.util.StringUtils;
import icu.funkye.redispike.protocol.RedisRequest;
import icu.funkye.redispike.protocol.RedisResponse;
import icu.funkye.redispike.protocol.response.BulkResponse;
import icu.funkye.redispike.protocol.response.IntegerResponse;

public class SRemRequest implements RedisRequest<String> {

    String       key;

    List<String>      bins;

    RedisResponse<String> response;

    public SRemRequest(List<String> params) {
        this.key = params.remove(0);
        if (params.isEmpty()) {
            BulkResponse bulkResponse = new BulkResponse();
            bulkResponse.setError("ERR wrong number of arguments for 'srem' command");
            this.response = bulkResponse;
        } else {
            this.response = new IntegerResponse();
        }
        this.bins = params;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getBins() {
        return bins;
    }

    @Override
    public void setResponse(String data) {
        this.response.setData(data);
    }

    @Override
    public RedisResponse<String> getResponse() {
        return response;
    }

}
