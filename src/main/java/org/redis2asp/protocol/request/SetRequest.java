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
package org.redis2asp.protocol.request;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.redis2asp.protocol.RedisRequest;
import org.redis2asp.protocol.RedisResponse;
import org.redis2asp.protocol.response.BulkResponse;

public class SetRequest implements RedisRequest<byte[]> {

    final String key;

    final String value;

    TtlType      ttlType;

    Long         ttl;

    Operate      operate;

    BulkResponse response = new BulkResponse();

    public SetRequest(String key, String value, List<String> params) {
        this.key = key;
        this.value = value;
        if (params.contains("NX")) {
            this.operate = Operate.NX;
        } else if (params.contains("XX")) {
            this.operate = Operate.XX;
        }
        if (params.contains("EX")) {
            this.ttlType = TtlType.EX;
            this.ttl = Long.parseLong(params.get(params.indexOf("EX") + 1));
        } else if (params.contains("PX")) {
            this.ttlType = TtlType.PX;
            this.ttl = Long.parseLong(params.get(params.indexOf("PX") + 1));
        }
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public TtlType getTtlType() {
        return ttlType;
    }

    public Long getTtl() {
        return ttl;
    }

    public Operate getOperate() {
        return operate;
    }

    @Override
    public void setResponse(byte[] data) {
        this.response.setData(data);
    }

    @Override
    public RedisResponse<byte[]> getResponse() {
        return response;
    }

    public enum TtlType {
        EX, PX
    }

    public enum Operate {
        NX, XX
    }

}
