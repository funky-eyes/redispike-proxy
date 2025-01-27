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

import java.util.List;
import java.util.Map;

import icu.funkye.redispike.protocol.AbstractRedisRequest;
import icu.funkye.redispike.protocol.RedisResponse;
import icu.funkye.redispike.protocol.request.conts.Operate;
import icu.funkye.redispike.protocol.response.BulkResponse;
import icu.funkye.redispike.protocol.response.IntegerResponse;
import icu.funkye.redispike.util.CollectionUtils;

public class HSetRequest extends AbstractRedisRequest<String> {

    final String              originalCommand;

    final String              key;

    final Map<String, String> kv;

    Operate                   operate;

    RedisResponse<String>     response;

    public HSetRequest(List<String> params, boolean flush) {
        this.flush = flush;
        this.originalCommand = params.remove(0);
        this.key = params.remove(0);
        this.kv = CollectionUtils.arrayToMap(params);
        if (originalCommand.contains("nx")) {
            this.operate = Operate.NX;
        } else if (params.contains("xx")) {
            this.operate = Operate.XX;
        }
        this.response = new IntegerResponse();
    }

    @Override
    public RedisResponse<String> getResponse() {
        return this.response;
    }

    @Override
    public void setResponse(String data) {
        this.response.setData(data);
    }

    public String getOriginalCommand() {
        return originalCommand;
    }

    public Map<String, String> getKv() {
        return kv;
    }

    public Operate getOperate() {
        return operate;
    }

    public void setOperate(Operate operate) {
        this.operate = operate;
    }

    public void setResponse(RedisResponse<String> response) {
        this.response = response;
    }

    public String getKey() {
        return key;
    }

    @Override
    public void setErrorResponse(String data) {
        this.response = new BulkResponse();
        ((BulkResponse) response).setError(data);
    }

    @Override
    public String toString() {
        return "HSetRequest{" + "originalCommand='" + originalCommand + '\'' + ", key='" + key + '\'' + ", kv=" + kv
               + ", operate=" + operate + ", response=" + response + '}';
    }
}
