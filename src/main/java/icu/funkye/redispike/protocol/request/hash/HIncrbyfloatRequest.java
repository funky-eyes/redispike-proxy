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
package icu.funkye.redispike.protocol.request.hash;

import icu.funkye.redispike.protocol.AbstractRedisRequest;
import icu.funkye.redispike.protocol.RedisResponse;
import icu.funkye.redispike.protocol.response.BulkResponse;
import icu.funkye.redispike.protocol.response.IntegerResponse;

public class HIncrbyfloatRequest extends AbstractRedisRequest<String> {

    final String key;

    final String field;

    final String value;

    BulkResponse response = new BulkResponse();

    public HIncrbyfloatRequest(String key, String field, String value, boolean flush) {
        this.flush = flush;
        this.key = key;
        this.value = value;
        this.field = field;
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

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public void setResponse(BulkResponse response) {
        this.response = response;
    }

}
