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
import icu.funkye.redispike.protocol.RedisResponse;
import icu.funkye.redispike.protocol.request.conts.Operate;
import icu.funkye.redispike.protocol.request.conts.TtlType;
import icu.funkye.redispike.protocol.response.BulkResponse;
import icu.funkye.redispike.protocol.response.IntegerResponse;
import icu.funkye.redispike.protocol.AbstractRedisRequest;

public class SetRequest extends AbstractRedisRequest<String> {

    final String          originalCommand;

    final String          key;

    final String          value;

    TtlType               ttlType;

    Long                  ttl;

    Operate               operate;

    RedisResponse<String> response;

    public SetRequest(List<String> params, boolean flush) {
        this.flush = flush;
        this.originalCommand = params.get(0);
        this.key = params.get(1);
        this.value = params.get(2);
        if (params.contains("nx")) {
            this.operate = Operate.NX;
        } else if (params.contains("xx")) {
            this.operate = Operate.XX;
        }
        if (params.contains("ex")) {
            this.ttlType = TtlType.EX;
            this.ttl = Long.parseLong(params.get(params.indexOf("ex") + 1));
        } else if (params.contains("px")) {
            this.ttlType = TtlType.PX;
            this.ttl = Long.parseLong(params.get(params.indexOf("px") + 1));
        }
        if (originalCommand.contains("nx")) {
            this.response = new IntegerResponse();
        } else {
            this.response = new BulkResponse();
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
    public void setResponse(String data) {
        this.response.setData(data);
    }

    @Override
    public RedisResponse<String> getResponse() {
        return response;
    }

    public String getOriginalCommand() {
        return originalCommand;
    }

    @Override
    public String toString() {
        return "SetRequest{" + "originalCommand='" + originalCommand + '\'' + ", key='" + key + '\'' + ", value='"
               + value + '\'' + ", ttlType=" + ttlType + ", ttl=" + ttl + ", operate=" + operate + ", response="
               + response + '}';
    }
}
